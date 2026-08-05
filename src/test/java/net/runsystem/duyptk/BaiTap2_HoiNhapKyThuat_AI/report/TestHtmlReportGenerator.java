package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.report;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@SuppressWarnings("PMD.TooManyMethods")
public final class TestHtmlReportGenerator {
    private static final int EXPECTED_ARGUMENT_COUNT = 3;
    private static final String AUTH_MODULE = "auth_authz";
    private static final String ORGANIZATION_MODULE = "organization";
    private static final String VOCAB_MODULE = "vocabulary_management";
    private static final String BASE_PACKAGE_PREFIX = "net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI";
    private static final String EXPECTED_ARGUMENTS_MESSAGE =
            "Expected arguments: <junitXmlDirectory> <jacocoXmlFile> <htmlOutputFile>";

    private TestHtmlReportGenerator() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != EXPECTED_ARGUMENT_COUNT) {
            throw new IllegalArgumentException(EXPECTED_ARGUMENTS_MESSAGE);
        }

        Path xmlDirectory = Path.of(args[0]);
        Path jacocoXml = Path.of(args[1]);
        Path htmlOutput = Path.of(args[2]);
        List<TestCaseResult> testCases = readTestCases(xmlDirectory);
        ReportData reportData = new ReportData(testCases, readCoverage(jacocoXml, testCases));
        String html = buildHtml(reportData);

        Files.createDirectories(htmlOutput.getParent());
        Files.writeString(htmlOutput, html, StandardCharsets.UTF_8);
    }

    private static List<TestCaseResult> readTestCases(Path xmlDirectory) throws Exception {
        if (!Files.isDirectory(xmlDirectory)) {
            return List.of();
        }

        List<TestCaseResult> results = new ArrayList<>();
        try (Stream<Path> files = Files.list(xmlDirectory)) {
            List<Path> xmlFiles = files
                    .filter(path -> path.getFileName().toString().startsWith("TEST-"))
                    .filter(path -> path.getFileName().toString().endsWith(".xml"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();

            for (Path xmlFile : xmlFiles) {
                results.addAll(readTestCasesFromFile(xmlFile));
            }
        }
        return results;
    }

    private static List<TestCaseResult> readTestCasesFromFile(Path xmlFile) throws Exception {
        Document document = newDocument(xmlFile);
        Element suite = document.getDocumentElement();
        NodeList testCases = suite.getElementsByTagName("testcase");
        List<TestCaseResult> results = new ArrayList<>();

        for (int index = 0; index < testCases.getLength(); index++) {
            Element testCase = (Element) testCases.item(index);
            String className = testCase.getAttribute("classname");
            String methodName = testCase.getAttribute("name");
            results.add(new TestCaseResult(
                    inferModule(className),
                    className,
                    methodName,
                    testCase.getAttribute("time"),
                    status(testCase)));
        }
        return results;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static CoverageSummary readCoverage(Path jacocoXml, List<TestCaseResult> testCases) throws Exception {
        if (!Files.isRegularFile(jacocoXml)) {
            return CoverageSummary.empty();
        }

        Document document = newDocument(jacocoXml);
        Element report = document.getDocumentElement();
        List<Element> moduleClasses = moduleClasses(report, testCases);
        Map<String, CoverageCounter> coverageCounters = new LinkedHashMap<>();

        for (Element moduleClass : moduleClasses) {
            NodeList classCounters = moduleClass.getChildNodes();
            for (int index = 0; index < classCounters.getLength(); index++) {
                if (classCounters.item(index) instanceof Element counter
                        && "counter".equals(counter.getTagName())) {
                    String type = counter.getAttribute("type");
                    coverageCounters.merge(
                            type,
                            new CoverageCounter(
                                    type,
                                    parseInt(counter.getAttribute("covered")),
                                    parseInt(counter.getAttribute("missed"))),
                            CoverageCounter::plus);
                }
            }
        }

        return new CoverageSummary(coverageCounters);
    }

    private static List<Element> moduleClasses(Element report, List<TestCaseResult> testCases) {
        List<String> classPrefixes = testCases.stream()
                .map(TestCaseResult::module)
                .distinct()
                .flatMap(module -> coverageClassPrefixes(module).stream())
                .toList();
        NodeList classes = report.getElementsByTagName("class");
        List<Element> moduleClasses = new ArrayList<>();

        for (int index = 0; index < classes.getLength(); index++) {
            Element moduleClass = (Element) classes.item(index);
            String className = moduleClass.getAttribute("name");
            if (classPrefixes.stream().anyMatch(className::startsWith)) {
                moduleClasses.add(moduleClass);
            }
        }
        return moduleClasses;
    }

    private static List<String> coverageClassPrefixes(String module) {
        if (AUTH_MODULE.equals(module)) {
            return List.of(BASE_PACKAGE_PREFIX + "/service/auth/UserService");
        }
        if (VOCAB_MODULE.equals(module)) {
            return List.of(
                    BASE_PACKAGE_PREFIX + "/controller/VocabController",
                    BASE_PACKAGE_PREFIX + "/domain/requestDTO/ReqCreateVocabDTO",
                    BASE_PACKAGE_PREFIX + "/domain/requestDTO/ReqUpdateVocabDTO",
                    BASE_PACKAGE_PREFIX + "/domain/responseDTO/ResVocabBulkImportDTO",
                    BASE_PACKAGE_PREFIX + "/domain/responseDTO/ResVocabBulkImportItemDTO",
                    BASE_PACKAGE_PREFIX + "/domain/responseDTO/ResVocabDTO",
                    BASE_PACKAGE_PREFIX + "/domain/externalDTO/VocabAutomationResult",
                    BASE_PACKAGE_PREFIX + "/domain/table/Vocab",
                    BASE_PACKAGE_PREFIX + "/service/vocab/VocabAudioService",
                    BASE_PACKAGE_PREFIX + "/service/vocab/VocabBulkImportService",
                    BASE_PACKAGE_PREFIX + "/service/vocab/FreeDictionaryVocabAutomationService",
                    BASE_PACKAGE_PREFIX + "/service/vocab/VocabLookupService",
                    BASE_PACKAGE_PREFIX + "/service/vocab/VocabValidationService",
                    BASE_PACKAGE_PREFIX + "/service/vocab/Vocab");
        }
        if (ORGANIZATION_MODULE.equals(module)) {
            return List.of(
                    BASE_PACKAGE_PREFIX + "/controller/OrganizationController",
                    BASE_PACKAGE_PREFIX + "/domain/requestDTO/ReqBulkAddVocabToSetDTO",
                    BASE_PACKAGE_PREFIX + "/domain/requestDTO/ReqCreateFolderDTO",
                    BASE_PACKAGE_PREFIX + "/domain/requestDTO/ReqCreateVocabSetDTO",
                    BASE_PACKAGE_PREFIX + "/domain/responseDTO/ResItemDTO",
                    BASE_PACKAGE_PREFIX + "/domain/responseDTO/ResVocabSetBulkAddDTO",
                    BASE_PACKAGE_PREFIX + "/domain/responseDTO/ResVocabSetBulkAddItemDTO",
                    BASE_PACKAGE_PREFIX + "/domain/responseDTO/ResVocabSetSummaryDTO",
                    BASE_PACKAGE_PREFIX + "/domain/responseDTO/ResVocabSetVocabDTO",
                    BASE_PACKAGE_PREFIX + "/domain/table/Folder",
                    BASE_PACKAGE_PREFIX + "/domain/table/Item",
                    BASE_PACKAGE_PREFIX + "/domain/table/ItemType",
                    BASE_PACKAGE_PREFIX + "/domain/table/VocabSet",
                    BASE_PACKAGE_PREFIX + "/service/organization/OrganizationItemLookupService",
                    BASE_PACKAGE_PREFIX + "/service/organization/OrganizationItemNameValidationService",
                    BASE_PACKAGE_PREFIX + "/service/organization/OrganizationService",
                    BASE_PACKAGE_PREFIX + "/service/organization/VocabSetMembershipService");
        }
        return List.of();
    }

    private static Document newDocument(Path xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        String xmlContent = Files.readString(xmlFile, StandardCharsets.UTF_8)
                .replaceFirst("<!DOCTYPE[^>]*>", "");
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xmlContent)));
    }

    private static String buildHtml(ReportData reportData) {
        TestTotals totals = totals(reportData.testCases());
        CoverageCounter lineCoverage = reportData.coverageSummary().counter("LINE");
        String moduleRows = moduleRows(reportData.testCases());
        String testRows = reportData.testCases().stream()
                .map(TestHtmlReportGenerator::testRow)
                .collect(Collectors.joining());
        String coverageRows = reportData.coverageSummary().counters().values().stream()
                .map(TestHtmlReportGenerator::coverageRow)
                .collect(Collectors.joining());

        return """
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Unit Test Report</title>
                    <style>
                        body{font-family:Arial,sans-serif;color:#172033;margin:32px;background:#f8fafc;}
                        main{max-width:1120px;margin:0 auto;}
                        h1{font-size:28px;margin:0 0 6px;}
                        h2{font-size:18px;margin:28px 0 10px;}
                        p{margin:0;color:#526174;}
                        table{width:100%%;border-collapse:collapse;background:#fff;border:1px solid #d8dee9;}
                        th,td{padding:10px;border-bottom:1px solid #e5e9f0;text-align:left;font-size:14px;}
                        th{background:#eef2f7;color:#293548;}
                        .summary{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px;margin:20px 0;}
                        .metric{background:#fff;border:1px solid #d8dee9;padding:14px;}
                        .metric strong{display:block;font-size:24px;color:#121826;}
                        .status-pass{color:#067647;font-weight:700;}
                        .status-fail{color:#b42318;font-weight:700;}
                        .muted{color:#667085;}
                    </style>
                </head>
                <body>
                <main>
                    <h1>Unit Test Report</h1>
                    <p>Generated from JUnit XML and JaCoCo coverage results.</p>

                    <section class="summary">
                        <div class="metric"><span>Total tests</span><strong>%d</strong></div>
                        <div class="metric"><span>Passed</span><strong>%d</strong></div>
                        <div class="metric"><span>Failed/Error</span><strong>%d</strong></div>
                        <div class="metric"><span>module line coverage</span><strong>%s</strong></div>
                    </section>

                    <h2>Modules Covered By Tests</h2>
                    <table>
                        <thead>
                            <tr><th>Module</th><th>Test Class Count</th><th>Test Count</th><th>Test Classes</th></tr>
                        </thead>
                        <tbody>%s</tbody>
                    </table>

                    <h2>Test Cases</h2>
                    <table>
                        <thead>
                            <tr><th>Module</th><th>Test Class</th><th>Test Method</th><th>Status</th><th>Time</th></tr>
                        </thead>
                        <tbody>%s</tbody>
                    </table>

                    <h2>Module Coverage</h2>
                    <table>
                        <thead><tr><th>Counter</th><th>Covered</th><th>Missed</th><th>Coverage</th></tr></thead>
                        <tbody>%s</tbody>
                    </table>
                </main>
                </body>
                </html>
                """.formatted(
                totals.total(),
                totals.passed(),
                totals.failed(),
                lineCoverage.percentage(),
                moduleRows,
                testRows,
                coverageRows);
    }

    private static String moduleRows(List<TestCaseResult> testCases) {
        Map<String, List<TestCaseResult>> byModule = testCases.stream()
                .collect(Collectors.groupingBy(TestCaseResult::module, LinkedHashMap::new, Collectors.toList()));

        return byModule.entrySet().stream()
                .map(entry -> {
                    List<String> classNames = entry.getValue().stream()
                            .map(TestCaseResult::className)
                            .distinct()
                            .sorted()
                            .toList();
                    return """
                            <tr>
                                <td>%s</td>
                                <td>%d</td>
                                <td>%d</td>
                                <td>%s</td>
                            </tr>
                            """.formatted(
                            escape(entry.getKey()),
                            classNames.size(),
                            entry.getValue().size(),
                            escape(String.join(", ", classNames)));
                })
                .collect(Collectors.joining());
    }

    private static String testRow(TestCaseResult result) {
        String statusClass = "PASSED".equals(result.status()) ? "status-pass" : "status-fail";
        return """
                <tr>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td class="%s">%s</td>
                    <td>%ss</td>
                </tr>
                """.formatted(
                escape(result.module()),
                escape(result.className()),
                escape(result.methodName()),
                statusClass,
                escape(result.status()),
                escape(result.time()));
    }

    private static String coverageRow(CoverageCounter counter) {
        return """
                <tr>
                    <td>%s</td>
                    <td>%d</td>
                    <td>%d</td>
                    <td>%s</td>
                </tr>
                """.formatted(
                escape(counter.type()),
                counter.covered(),
                counter.missed(),
                counter.percentage());
    }

    private static TestTotals totals(List<TestCaseResult> testCases) {
        int failed = (int) testCases.stream()
                .filter(testCase -> !"PASSED".equals(testCase.status()))
                .count();
        return new TestTotals(testCases.size(), testCases.size() - failed, failed);
    }

    private static String inferModule(String className) {
        if (className.contains(".service.UserServiceTests")) {
            return AUTH_MODULE;
        }
        if (className.contains(".service.VocabServiceTests")) {
            return VOCAB_MODULE;
        }
        if (className.contains(".service.organization.")) {
            return ORGANIZATION_MODULE;
        }
        return "unknown";
    }

    private static String status(Element testCase) {
        if (testCase.getElementsByTagName("failure").getLength() > 0) {
            return "FAILED";
        }
        if (testCase.getElementsByTagName("error").getLength() > 0) {
            return "ERROR";
        }
        if (testCase.getElementsByTagName("skipped").getLength() > 0) {
            return "SKIPPED";
        }
        return "PASSED";
    }

    private static int parseInt(String value) {
        return value == null || value.isBlank() ? 0 : Integer.parseInt(value);
    }

    private static String escape(String value) {
        String safeValue = value == null ? "" : value;
        return safeValue
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private record ReportData(List<TestCaseResult> testCases, CoverageSummary coverageSummary) {
    }

    private record TestCaseResult(String module, String className, String methodName, String time, String status) {
    }

    private record TestTotals(int total, int passed, int failed) {
    }

    private record CoverageSummary(Map<String, CoverageCounter> counters) {
        private static CoverageSummary empty() {
            return new CoverageSummary(Map.of());
        }

        private CoverageCounter counter(String type) {
            return counters.getOrDefault(type, new CoverageCounter(type, 0, 0));
        }
    }

    private record CoverageCounter(String type, int covered, int missed) {
        private CoverageCounter plus(CoverageCounter other) {
            return new CoverageCounter(type, covered + other.covered(), missed + other.missed());
        }

        private String percentage() {
            int total = covered + missed;
            if (total == 0) {
                return "N/A";
            }
            return String.format(Locale.ROOT, "%.2f%%", covered * 100.0 / total);
        }
    }
}
