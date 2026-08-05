plugins {
	java
	id("org.springframework.boot") version "4.0.7"
	id("io.spring.dependency-management") version "1.1.7"

	checkstyle
	jacoco
	pmd
}

group = "net.runsystem.duyptk"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

// =====================================================
// Checkstyle
// =====================================================

checkstyle {
    toolVersion = "10.26.1"

    // File chứa các rule của Checkstyle
    configFile = file("$rootDir/config/checkstyle/checkstyle.xml")

    // Có thể bật/tắt việc fail build khi có warning
    isIgnoreFailures = false
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// =====================================================
// PMD
// =====================================================

pmd {
    toolVersion = "7.16.0"

    // Nếu có lỗi PMD thì fail build
    isIgnoreFailures = false

    // Hiển thị rule bị vi phạm trên console
    isConsoleOutput = true

    ruleSetFiles = files("$rootDir/config/pmd/ruleset.xml")
    ruleSets = emptyList()
}

tasks.withType<Pmd>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

repositories {
	mavenCentral()
}

configurations.configureEach {
	exclude(group = "org.slf4j", module = "slf4j-simple")
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.apache.poi:poi-ooxml:5.4.1")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("com.mysql:mysql-connector-j")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("com.h2database:h2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")

	// Google TTS
	implementation(platform("com.google.cloud:libraries-bom:26.83.0"))
	implementation("com.google.cloud:google-cloud-texttospeech")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

jacoco {
	toolVersion = "0.8.13"
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		html.required.set(true)
		csv.required.set(false)
	}
}

val testHtmlReport by tasks.registering(JavaExec::class) {
	group = "verification"
	description = "Generate an HTML report from JUnit XML results and JaCoCo coverage."
	dependsOn(tasks.test, tasks.jacocoTestReport)
	classpath = sourceSets["test"].runtimeClasspath
	mainClass.set("net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.report.TestHtmlReportGenerator")
	args(
		layout.buildDirectory.dir("test-results/test").get().asFile.absolutePath,
		layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml").get().asFile.absolutePath,
		layout.buildDirectory.file("reports/tests/auth-unit-test-report.html").get().asFile.absolutePath
	)
}
