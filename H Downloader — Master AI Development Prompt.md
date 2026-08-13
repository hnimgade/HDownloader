# H DOWNLOADER — MASTER ANDROID DEVELOPMENT PROMPT

You are a senior Android architect, Kotlin engineer, Jetpack Compose developer, UI/UX designer, networking engineer, media-processing engineer, QA engineer, and security engineer.

Your task is to design and build a production-quality Android application called:

**H Downloader**

The application is a modern, professional download manager and media-management application inspired by the functional concepts found in professional download managers such as IDM, 1DM, ADM and SnapTube.


==================================================
1. PRODUCT VISION
==================================================

Build H Downloader as a premium Android download manager that allows users to:

1. Download files.
2. Download images.
3. Download authorized video content.
4. Download authorized audio content.
5. Manage multiple downloads.
6. Pause/resume downloads.
7. Queue downloads.
8. Perform multi-threaded HTTP downloads where supported.
9. Resume interrupted downloads.
10. Detect downloadable media from supported sources.
11. Download shared URLs.
12. Receive URLs through Android Share Sheet.
13. Detect copied URLs when the user explicitly enables clipboard monitoring.
14. Browse the web inside the application.
15. Play downloaded videos and audio.
16. Organize downloaded media.
17. Convert supported local media using FFmpeg where licensing and technical constraints permit.
18. Support HLS streams where downloading is authorized.
19. Maintain download history.
20. Provide batch-download functionality.
21. Provide playlists/queues.
22. Provide storage management.
23. Provide professional settings and customization.

The application must feel:

- Fast
- Stable
- Premium
- Modern
- Minimal
- Responsive
- Professional
- Privacy-conscious
- Reliable for large downloads

==================================================
2. TECHNOLOGY STACK
==================================================

Use:

- Kotlin
- Android Gradle Plugin
- Jetpack Compose
- Material 3
- Kotlin Coroutines
- Kotlin Flow / StateFlow
- Navigation Compose
- Room
- DataStore
- WorkManager
- Android MediaStore
- Android Notification APIs
- Media3 / ExoPlayer for media playback
- OkHttp for HTTP networking
- Kotlin Serialization where appropriate
- Hilt for dependency injection
- Paging where appropriate
- FFmpeg integration only where technically and legally appropriate

Use current stable Android APIs.

Use a modern Android architecture.

Minimum target architecture:

UI
↓
ViewModel
↓
Use Cases
↓
Repositories
↓
Data Sources
↓
Database / Network / Download Engine / Media Engine

==================================================
3. ARCHITECTURE
==================================================

Use Clean Architecture with clear module boundaries.

Recommended structure:

app/
core/
    common/
    designsystem/
    model/
    database/
    network/
    storage/
    notifications/
    media/
    download/
    security/
feature/
    home/
    downloads/
    browser/
    media/
    history/
    settings/
    playlist/
    converter/
    storage/
    status/
    about/

Use interfaces between major subsystems.

Do not place business logic directly inside Composable functions.

Composable functions must remain primarily responsible for UI rendering and UI events.

==================================================
4. DOWNLOAD ENGINE
==================================================

The Download Engine is the most important subsystem.

Implement:

- HTTP/HTTPS downloads
- Redirect handling
- Range requests
- Resume support
- Retry support
- Download queue
- Concurrent downloads
- Configurable maximum concurrent downloads
- Configurable connections/chunks where supported
- Download speed calculation
- ETA calculation
- Progress calculation
- File-size detection
- MIME type detection
- Filename detection
- Extension detection
- Storage validation
- Network validation
- Download cancellation
- Pause/resume
- Failed-download recovery
- Partial-file handling
- Integrity/error handling
- Duplicate filename handling
- Temporary-file handling
- Completed-file finalization

Never assume that every server supports byte-range requests.

Detect server capabilities before enabling segmented downloading.

Do not corrupt files when multiple chunks are used.

Implement a robust download state machine.

Possible states:

QUEUED
PREPARING
DOWNLOADING
PAUSED
COMPLETING
COMPLETED
FAILED
CANCELLED

Persist download state in Room.

==================================================
5. DOWNLOAD DATABASE
==================================================

Create Room entities for at least:

DownloadEntity
DownloadPartEntity
DownloadHistoryEntity
CategoryEntity
PlaylistEntity
PlaylistItemEntity
BrowserHistoryEntity
BookmarkEntity
MediaEntity
AppSettingEntity

DownloadEntity should contain appropriate fields such as:

- id
- url
- finalUrl
- fileName
- filePath / MediaStore URI
- mimeType
- extension
- totalBytes
- downloadedBytes
- status
- speed
- eta
- createdAt
- startedAt
- completedAt
- errorMessage
- categoryId
- thumbnailUri
- isPaused
- isSelected
- retryCount
- supportsRange
- connectionCount

Do not store secrets or authentication tokens unnecessarily.

==================================================
6. HOME SCREEN
==================================================

Create a premium Material 3 dark Home screen.

Main elements:

- App logo
- Greeting/header
- Smart URL/search input
- Add URL button
- Download URL action
- Playlist action
- Recent downloads
- Active downloads
- Download speed summary
- Storage summary
- Quick actions
- Recent media

Use:

- Rounded cards
- Material 3 components
- Subtle elevation
- Minimal gradients
- Smooth animations
- Professional typography
- Responsive layouts

Do not overcrowd the screen.

Primary action must be obvious.

==================================================
7. ADD URL DIALOG
==================================================

When the user selects Add URL:

Display a modern bottom sheet/dialog.

Fields:

- URL
- Filename
- Category
- Download location

Options:

- Multi-thread download
- Wi-Fi only
- Start immediately
- Add to queue

Buttons:

CANCEL
ADD DOWNLOAD
START DOWNLOAD

Validate URL before starting.

Display useful validation errors.

==================================================
8. MEDIA DETECTION SCREEN
==================================================

For supported and authorized media sources, create a media detection screen.

Show:

- Thumbnail
- Title
- Source
- Duration if available
- Available video formats
- Available audio formats
- Resolution
- Codec when available
- Container
- Estimated file size
- Audio-only option

Example choices:

Video:

1080p
720p
480p
360p

Audio:

320 kbps
192 kbps
128 kbps

Formats may include:

MP4
WEBM
M4A
MP3

Only display formats actually available from the source.

Never fabricate quality options.

Provide:

DOWNLOAD
ADD TO QUEUE

==================================================
9. DOWNLOADS SCREEN
==================================================

Create a professional download manager screen.

Tabs:

ALL
DOWNLOADING
QUEUED
COMPLETED
FAILED

Each download card should show:

- Thumbnail/icon
- Filename
- File size
- Downloaded amount
- Progress
- Speed
- ETA
- Status
- Pause/resume
- Cancel
- Retry

Support:

- Swipe actions
- Long press
- Multi-select
- Delete
- Share
- Open
- Retry
- Pause
- Resume

Expandable cards may display:

- Download URL
- MIME type
- Connection count
- Created date
- Destination
- Error message

==================================================
10. DOWNLOAD QUEUE
==================================================

Create a queue management bottom sheet.

Allow:

- Reorder
- Pause all
- Resume all
- Cancel all
- Start selected
- Delete selected

Allow configurable maximum simultaneous downloads.

Persist queue order.

==================================================
11. MULTI-THREAD DOWNLOADS
==================================================

Implement segmented downloading only when the HTTP server supports range requests.

For example:

File
|
+-- Part 1
+-- Part 2
+-- Part 3
+-- Part 4

Each part must track:

- Start byte
- End byte
- Current byte
- Status
- Retry count

Merge safely after completion.

Avoid excessive connection counts.

Provide a setting for maximum connections.

==================================================
12. BACKGROUND DOWNLOADS
==================================================

Downloads must continue when the Activity is not visible.

Use an Android-compatible background execution strategy.

Show persistent notification for active downloads where required.

Notification should include:

- Filename
- Progress
- Speed
- Pause/resume action
- Cancel action

Show a completion notification when the download finishes.

Handle:

- App backgrounding
- Device reboot where appropriate
- Network loss
- Network recovery
- Low storage
- Battery restrictions

==================================================
13. ANDROID SHARE SHEET
==================================================

Register H Downloader as an Android Share target for supported URL/text content.

Flow:

Other application
→ Share
→ H Downloader
→ Analyze URL
→ Show download options
→ Download

Do not silently start downloads without user confirmation unless explicitly configured by the user.

==================================================
14. CLIPBOARD
==================================================

Implement optional clipboard URL detection.

This feature must be opt-in.

If the user copies a URL and clipboard access is available:

Show a lightweight popup:

"Download detected URL?"

Actions:

DOWNLOAD
DISMISS

Do not constantly access clipboard unnecessarily.

Respect Android privacy restrictions.

==================================================
15. BUILT-IN BROWSER
==================================================

Create a dedicated Browser screen.

Components:

- Address bar
- Search
- Back
- Forward
- Refresh
- Home
- Tabs
- Bookmark
- Share
- Download detection
- Bottom toolbar

Browser should feel similar to a modern mobile browser while being optimized for legitimate media downloading.

Do not implement DRM circumvention.

Do not bypass authentication.

Do not inject code into websites to defeat security mechanisms.

For supported pages, identify downloadable resources using legitimate/technically appropriate methods.

==================================================
16. BROWSER DOWNLOAD DETECTION
==================================================

When an authorized downloadable media resource is detected:

Show a bottom sheet:

Media detected

Thumbnail
Title
Type
Size

DOWNLOAD
IGNORE

Avoid aggressive popups.

Allow users to disable media detection.

==================================================
17. HLS
==================================================

Support HLS only for streams that can legally and technically be downloaded.

Support:

.m3u8

Implement:

- Playlist parsing
- Variant selection
- Resolution selection
- Segment download
- Retry
- Segment ordering
- Finalization

If encryption/DRM is present and the content requires bypassing protection, DO NOT bypass it.

Clearly report unsupported protected streams.

==================================================
18. MEDIA PLAYER
==================================================

Use Android Media3 / ExoPlayer.

Video player:

- Play/pause
- Seek
- Fullscreen
- Playback speed
- Picture-in-picture
- Audio controls
- Subtitle support where available
- Resume position
- Lock controls

Audio player:

- Play/pause
- Next/previous
- Seek
- Queue
- Shuffle
- Repeat
- Background playback
- Media notification
- Lock-screen controls

==================================================
19. LOCAL MEDIA LIBRARY
==================================================

Create categories:

Videos
Music
Images
Documents
Other

Display:

- Thumbnail
- Filename
- Size
- Duration
- Date
- Location

Actions:

- Open
- Share
- Rename
- Delete
- Move
- Add to playlist
- Details

Use MediaStore where appropriate instead of relying on deprecated unrestricted filesystem access.

==================================================
20. PLAYLISTS
==================================================

Implement local playlists.

Features:

- Create playlist
- Rename
- Delete
- Add media
- Remove media
- Reorder
- Play all
- Download selected
- Download all where supported

==================================================
21. DOWNLOAD HISTORY
==================================================

Maintain a history screen.

Display:

- Filename
- Date
- Source
- Size
- Status

Actions:

- Download again
- Delete history
- Open
- Share

Allow clearing history.

==================================================
22. BATCH DOWNLOAD
==================================================

Create batch download UI.

Allow multiple URLs.

Example:

URL 1
URL 2
URL 3
URL 4

Actions:

- Select all
- Remove
- Set category
- Set destination
- Start all
- Add to queue

Validate every URL individually.

==================================================
23. SETTINGS
==================================================

Create a premium settings screen.

Sections:

DOWNLOADS
- Default download location
- Maximum concurrent downloads
- Maximum connections
- Auto retry
- Wi-Fi only
- Auto start
- Notification settings

BROWSER
- Homepage
- Search engine
- User agent
- Media detection
- JavaScript settings

CLIPBOARD
- Enable/disable detection

MEDIA
- Default video quality
- Default audio quality
- Player settings

APPEARANCE
- Light
- Dark
- System
- Dynamic colors

STORAGE
- Download location
- Storage usage
- Cache
- Cleanup

BATTERY
- Background download information
- Battery optimization guidance

PRIVACY
- Clear history
- Clear browser history
- Clear clipboard-related data

ABOUT
- Version
- Licenses
- Privacy policy
- Terms
- Open-source licenses

==================================================
24. STORAGE CLEANER
==================================================

Implement this as a secondary feature, not the core product.

Allow users to inspect:

- Large files
- Downloaded files
- Cache
- Duplicate files

Never delete anything automatically without confirmation.

==================================================
25. DUPLICATE FILE FINDER
==================================================

Detect duplicates using safe file metadata and, when appropriate, cryptographic hashes.

Display:

Group
File A
File B
File C

Allow:

KEEP
DELETE SELECTED

Never automatically delete files.

==================================================
26. MEDIA CONVERTER
==================================================

Create an optional local-media converter.

Possible conversions:

Video → Audio
Supported video → MP4
Audio format conversion where supported

Use FFmpeg only if the chosen Android integration is properly licensed and maintained.

Clearly separate conversion from downloading.

Never use conversion as a mechanism to bypass DRM.

==================================================
27. STATUS SAVER
==================================================

If implemented, make this a separate optional module.

Support only user-accessible media and comply with the source application's storage/access rules and Android permissions.

Provide:

Images
Videos

Actions:

Save
Share
Delete

Do not bypass application security or access restrictions.

==================================================
28. OPTIONAL UTILITY FEATURES
==================================================

The original reference concept contains additional utility-style features such as:

- Storage cleaner
- Duplicate finder
- App/media management
- Device optimization concepts

Treat these as optional modules.

Do NOT make "RAM booster", "battery booster", "phone optimizer", or similar claims unless the feature actually provides meaningful functionality.

Do not use misleading system-performance claims.

==================================================
29. UI DESIGN
==================================================

Use Material 3.

Visual direction:

- Premium dark theme
- Black/dark surfaces
- Deep blue/purple primary
- Cyan/neon-blue accent used sparingly
- Rounded cards
- 12–20dp corner radius
- Strong visual hierarchy
- Minimal gradients
- Subtle shadows
- Clean icons
- Smooth transitions

Avoid:

- Excessive glassmorphism
- Excessive neon
- Huge text
- Crowded dashboards
- Too many floating buttons
- Fake performance metrics
- Unnecessary animations

The UI should look like a professional Android application rather than a gaming interface.

==================================================
30. RESPONSIVE DESIGN
==================================================

Support:

- Phones
- Tablets
- Foldables
- Portrait
- Landscape

Use adaptive layouts.

For larger screens:

NavigationRail / NavigationDrawer may replace bottom navigation where appropriate.

Download cards should adapt to available width.

Browser should support large-screen layouts.

==================================================
31. NAVIGATION
==================================================

Primary navigation:

Home
Downloads
Browser
Settings

Secondary screens:

Media Detection
Player
History
Playlists
Storage
Converter
About

Use Navigation Compose.

Handle deep links safely.

==================================================
32. ANIMATIONS
==================================================

Implement subtle animations for:

- Download progress
- Card expansion
- Queue changes
- Bottom sheets
- Loading
- Download completion
- Swipe actions
- Navigation

Animations must not reduce performance.

==================================================
33. ACCESSIBILITY
==================================================

Support:

- Content descriptions
- Screen readers
- Large fonts
- Touch target sizes
- Contrast
- Reduced motion
- Keyboard navigation where appropriate

==================================================
34. SECURITY
==================================================

Follow secure Android practices.

Do not:

- Hardcode API secrets
- Store passwords in plain text
- Log sensitive URLs unnecessarily
- Store authentication cookies unnecessarily
- Request unrelated permissions
- Access private application data
- Bypass Android security controls

Use encrypted storage when sensitive information genuinely needs to be stored.

==================================================
35. PERMISSIONS
==================================================

Request only permissions genuinely required.

Prefer:

- MediaStore
- Storage Access Framework
- Android Share APIs
- Notification permission where needed

Do not request broad storage permissions unnecessarily.

Explain permission usage clearly.

==================================================
36. ERROR HANDLING
==================================================

Every operation must have useful errors.

Examples:

No internet
Invalid URL
Unsupported format
Server does not support resume
Insufficient storage
Permission denied
Network timeout
HTTP error
Media unavailable
Protected content
Download corrupted
File already exists

Provide actionable messages.

Example:

"Server does not support resumable downloads. The file will be downloaded using a standard connection."

==================================================
37. OFFLINE BEHAVIOR
==================================================

The local download manager must remain usable without internet.

Users should still be able to:

- Browse downloaded files
- Play downloaded media
- View history
- Manage files
- Manage playlists
- Change settings

==================================================
38. PERFORMANCE
==================================================

Optimize for:

- Large files
- Large download queues
- Thousands of history records
- Low-memory devices
- Background execution
- Battery consumption

Do not update Compose UI on every byte received.

Throttle progress updates appropriately.

Use Flow and structured concurrency.

==================================================
39. TESTING
==================================================

Create:

Unit tests for:

- Download state machine
- URL validation
- Filename parsing
- MIME detection
- Progress calculation
- ETA calculation
- Queue ordering
- Retry behavior

Integration tests for:

- Database
- Download engine
- MediaStore
- Browser integration

UI tests for:

- Home
- Downloads
- Add URL
- Media detection
- Settings

Test:

- Network interruption
- Resume
- Pause
- Cancel
- Low storage
- Large files
- Server without range support
- Multiple simultaneous downloads

==================================================
40. LOGGING
==================================================

Create structured debug logging.

Never expose sensitive user information in production logs.

Provide useful diagnostics for:

- Download failures
- HTTP errors
- Storage failures
- Media detection errors

==================================================
41. PROJECT GENERATION STRATEGY
==================================================

Do NOT attempt to generate the entire application in one giant response if that would produce incomplete or non-compiling code.

Build incrementally.

PHASE 1:
Project setup
- Gradle
- Kotlin
- Compose
- Material 3
- Hilt
- Navigation
- Theme

PHASE 2:
Database
- Room
- Entities
- DAOs
- Repositories

PHASE 3:
Download engine
- HTTP
- Queue
- Progress
- Pause/resume
- Retry
- Persistence

PHASE 4:
Home + Downloads UI

PHASE 5:
Background downloads + notifications

PHASE 6:
Share Sheet + URL handling

PHASE 7:
Media detection

PHASE 8:
Browser

PHASE 9:
Media player

PHASE 10:
History + playlists

PHASE 11:
Settings

PHASE 12:
Storage tools

PHASE 13:
Converter

PHASE 14:
Testing + performance

PHASE 15:
Release preparation

==================================================
42. IMPORTANT CODING RULE
==================================================

After every implementation phase:

1. Compile the project.
2. Fix compilation errors.
3. Fix warnings that affect correctness.
4. Run relevant tests.
5. Verify navigation.
6. Verify state management.
7. Verify database migrations.
8. Only then continue to the next phase.

Never pretend that code compiles if it has not been validated.

When a dependency/version is uncertain, use the currently configured project's compatible version instead of inventing a version.

==================================================
43. FILE STRUCTURE
==================================================

Use a maintainable structure similar to:

com.hdownloader
|
+-- MainActivity.kt
|
+-- core
|   +-- common
|   +-- designsystem
|   +-- database
|   +-- network
|   +-- download
|   +-- media
|   +-- storage
|   +-- notifications
|   +-- security
|
+-- feature
    +-- home
    +-- downloads
    +-- browser
    +-- media
    +-- history
    +-- playlist
    +-- converter
    +-- storage
    +-- settings
    +-- about

Keep feature-specific code inside its feature module/package.

==================================================
44. DEVELOPMENT RULES
==================================================

Do not use placeholder TODO implementations for core functionality.

Do not create fake download progress.

Do not simulate successful downloads.

Do not hardcode fake media metadata.

If a feature cannot be implemented yet:

- Clearly identify the limitation.
- Create the correct interface/abstraction.
- Implement the safe portion.
- Leave a documented extension point.

==================================================
45. AI CODING BEHAVIOR
==================================================

You are responsible for maintaining a working codebase.

Before changing code:

1. Inspect the existing project.
2. Understand existing architecture.
3. Reuse existing components where appropriate.
4. Avoid duplicate implementations.
5. Make the smallest coherent change.
6. Compile/test the change.

When creating a new feature:

Explain briefly:

- Files to create
- Files to modify
- Architecture impact
- Dependencies required

Then implement it.

Do not overwrite working code unnecessarily.

==================================================
46. FIRST TASK
==================================================

Do NOT immediately implement every feature.

First create:

1. Project architecture
2. Gradle configuration
3. Hilt setup
4. Material 3 theme
5. Navigation
6. MainActivity
7. Home screen
8. Downloads screen
9. Browser screen shell
10. Settings screen
11. Room database foundation
12. Download model/state machine foundation

Make the initial project compile successfully.

Then stop and report:

- Files created
- Files modified
- Dependencies
- Architecture
- Compilation result
- Tests executed
- Remaining work

After that, continue phase-by-phase.

==================================================
47. PRODUCT IDENTITY
==================================================

Application name:

H Downloader

Do not use:

SnapTube
TubeMate
IDM
1DM
ADM

as the application name, package name, logo, or branding.

Create an original H Downloader visual identity.

Use a simple original download-arrow-based logo.

==================================================
48. FINAL QUALITY STANDARD
==================================================

The finished application should feel like a serious commercial Android application.

It must prioritize:

1. Download reliability
2. UI responsiveness
3. Background execution
4. Resume support
5. Queue management
6. Media organization
7. Privacy
8. Accessibility
9. Error handling
10. Maintainability

Do not sacrifice architecture for visual effects.

Do not sacrifice reliability for feature count.

Do not sacrifice legal/platform compliance for download compatibility.

START WITH PHASE 1 AND BUILD THE PROJECT INCREMENTALLY.
