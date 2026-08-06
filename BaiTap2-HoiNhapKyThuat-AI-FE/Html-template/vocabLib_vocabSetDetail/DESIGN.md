---
name: Vocab Library Design System
colors:
  surface: '#f8f9ff'
  surface-dim: '#d4dae6'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eef4ff'
  surface-container: '#e8eefa'
  surface-container-high: '#e2e9f4'
  surface-container-highest: '#dce3ee'
  on-surface: '#151c24'
  on-surface-variant: '#424655'
  inverse-surface: '#2a313a'
  inverse-on-surface: '#ebf1fd'
  outline: '#737687'
  outline-variant: '#c2c6d8'
  surface-tint: '#0055d5'
  primary: '#004bbf'
  on-primary: '#ffffff'
  primary-container: '#0061f2'
  on-primary-container: '#ebeeff'
  inverse-primary: '#b3c5ff'
  secondary: '#006686'
  on-secondary: '#ffffff'
  secondary-container: '#44c9fd'
  on-secondary-container: '#00516b'
  tertiary: '#6500ea'
  on-tertiary: '#ffffff'
  tertiary-container: '#7e3cff'
  on-tertiary-container: '#f4eaff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dae1ff'
  primary-fixed-dim: '#b3c5ff'
  on-primary-fixed: '#001849'
  on-primary-fixed-variant: '#003fa4'
  secondary-fixed: '#c0e8ff'
  secondary-fixed-dim: '#6fd2ff'
  on-secondary-fixed: '#001f2b'
  on-secondary-fixed-variant: '#004d66'
  tertiary-fixed: '#e9ddff'
  tertiary-fixed-dim: '#d0bcff'
  on-tertiary-fixed: '#23005c'
  on-tertiary-fixed-variant: '#5600ca'
  background: '#f8f9ff'
  on-background: '#151c24'
  surface-variant: '#dce3ee'
typography:
  headline-lg:
    fontFamily: Hanken Grotesk
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Hanken Grotesk
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Hanken Grotesk
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Hanken Grotesk
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Hanken Grotesk
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
  label-md:
    fontFamily: Hanken Grotesk
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
  label-sm:
    fontFamily: Hanken Grotesk
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  container-padding: 2rem
  stack-gap: 1.5rem
  inline-gap: 0.75rem
  grid-gutter: 1.5rem
  card-padding: 1.25rem
---

## Brand & Style

The design system is built for a professional, productivity-oriented educational platform. It prioritizes clarity, systematic organization, and a sense of academic reliability. The target audience includes students, educators, and corporate learners who require a high-density, functional interface for managing large datasets of information.

The visual style is **Corporate / Modern**. It utilizes a "Clean & Bright" aesthetic characterized by high-contrast typography, a rigid layout structure, and functional use of color to indicate hierarchy and action. The interface focuses on utility, using subtle borders and soft shadows to organize content without creating visual noise.

Key attributes:
- **Professionalism:** Precise alignment and systematic spacing.
- **Efficiency:** High information density optimized for rapid scanning.
- **Clarity:** Distinct separation between navigation, global actions, and primary content.

## Colors

The palette is anchored by a vibrant Primary Blue, used strategically for primary actions and brand recognition. A secondary Teal and Tertiary Purple are used for specialized actions like testing and file management to provide visual distinction between different toolsets.

The neutral scale is crucial for this design system. We use a cool-toned light gray for the global background to allow white content cards to "pop" via elevation. 

**Color Usage Rules:**
- **Primary (#0061f2):** Primary buttons, active navigation states, and folder highlights.
- **Success/Teal (#00ba94):** Creation and testing actions.
- **Surface:** Main workspace areas use pure white to ensure maximum legibility of data.
- **Border:** A consistent 1px solid border in a subtle cool-gray is used to define table rows and container boundaries.

## Typography

The design system exclusively uses **Hanken Grotesk** to maintain a modern, geometric, and professional feel. The type scale is designed for data-heavy environments, favoring smaller body sizes with generous line heights to prevent visual crowding.

**Hierarchical Logic:**
- **Headlines:** Use SemiBold (600) or Bold (700) weights. "Headline-lg" is reserved for page titles and branding.
- **Labels:** Use uppercase for table headers to create a distinct visual break from the tabular data.
- **Body:** The default interface size is 14px (body-md), while 13px (body-sm) is used for secondary metadata such as timestamps and descriptions.

## Layout & Spacing

The system follows a **Fixed Grid** philosophy for the main content area, centered within a fluid shell. It utilizes a sidebar + main content layout model.

- **Sidebar:** Fixed width (280px), containing navigation trees and utility zones.
- **Header:** Global search and user management pinned to the top.
- **Content Area:** Elements are grouped into white surfaces (cards). 
- **The 8px Rule:** All spacing between elements (paddings, margins, gutters) must be a multiple of 4px, with 8px and 16px being the most frequent increments.
- **Density:** The design maintains a "Medium" density—breathable enough for comfort but tight enough to show 10+ rows of data without scrolling on a standard laptop screen.

## Elevation & Depth

This design system uses a combination of **Tonal Layers** and **Ambient Shadows** to define the hierarchy of information.

- **Level 0 (Background):** The base layer uses a soft gray (#f2f5f9). No shadows.
- **Level 1 (Cards/Sidebar):** Primary containers are pure white with a subtle 1px border (#e3e6ec). A very soft, diffused shadow (0px 4px 12px rgba(0,0,0,0.03)) is applied to separate the workspace from the background.
- **Level 2 (Dropdowns/Modals):** Elements that float above the UI use a more pronounced shadow (0px 8px 24px rgba(0,0,0,0.08)) and a crisp border.
- **Interactive States:** Buttons use a slight inset shadow on 'active' states to simulate a physical press.

## Shapes

The design system uses a **Rounded** shape language to soften the corporate aesthetic and make the platform feel more approachable.

- **Standard Radius (0.5rem):** Used for buttons, input fields, and standard cards.
- **Large Radius (1rem):** Used for main layout containers or "Featured" cards.
- **Full Radius (Pill):** Used for status tags or "Chips" to distinguish them from interactive buttons.
- **Sidebar Selection:** Uses a single-sided or subtle rounded-rectangle highlight to indicate the active state in the navigation tree.

## Components

### Buttons
- **Primary:** Solid #0061f2 with white text. Rounded (8px). Includes a leading icon.
- **Secondary:** Outlined with primary color or soft gray border. 
- **Action (Ghost):** Used within table rows. Subtle border, light gray text, becomes primary blue on hover.
- **Success:** Solid #00ba94 for "Create" or "Test" actions.

### Input Fields
- **Search:** Large, full-width with a light background and a prominent leading search icon. 
- **Selects:** Used in pagination; include a chevron icon and a subtle border.

### Tables
- **Header:** Sticky, using `label-md` typography.
- **Rows:** Divided by 1px horizontal lines. High contrast for the "Name" column.
- **Hover State:** Entire row receives a very light blue (#f8faff) background tint on hover.

### Progress & Tags
- **Badges:** Small rounded rectangles with light tinted backgrounds (e.g., light blue for folder counts) to indicate quantity or status without overwhelming the text.

### Navigation Tree
- **Folders:** Uses a nested structure with chevron toggles. Active folders are highlighted with a light blue background and bold text.