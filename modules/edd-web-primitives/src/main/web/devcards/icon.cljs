(ns web.devcards.icon
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.devcards.utils :refer [apply-stiles]]

   [web.primitives.icons.core :refer
    [AccountBalanceIcon AccountBalanceWalletIcon AccountCircleIcon AddIcon  AdminPanelSettingsIcon
     AllColapseIcon AllExpandIcon ArrowDownwardIcon ArrowDropDownCircleIcon ArrowDropDownIcon
     ArrowDropUpIcon ArrowLeftIcon ArrowRightIcon ArrowUpwardIcon AssignmentIndIcon BlockIcon
     BusinessIcon CallMadeIcon CachedIcon ChatBubbleOutlineIcon ChatIcon CalculateIcon
     CheckCircleOutlineOutlinedIcon ChevronRightIcon ChromeReaderModeIcon ClearIcon CloseIcon
     ContentCopyIcon DeleteIcon DialpadIcon DirectionsRunIcon DoNotTouchIcon DownloadIcon EditIcon
     UploadIcon EmojiPeopleIcon EqualizerIcon ErrorOutlineIcon EventIcon ExpandLessIcon
     ExpandMoreIcon ExposureIcon FindInPageIcon FormatAlignLeftOutlinedIcon ForwardIcon GetAppIcon
     HelpOutlineIcon HomeIcon HomeOutlinedIcon InfoOutlinedIcon InfoIcon KeyboardTabIcon LaunchIcon
     LinkIcon LinkOffIcon LogoIcon HierarchyIcon ManageSearchIcon MarkunreadMailboxIcon MenuIcon
     MonetizationOnIcon MoreHorizIcon MoreVertIcon NoteAddIcon NoteOutlinedIcon
     NotificationsNoneIcon PeopleIcon PersonIcon PersonSearchIcon PictureAsPdfIcon
     PlaylistAddCheckIcon PlaylistAddIcon PublishIcon RefreshIcon RemoveIcon ReportProblemIcon
     SaveIcon SearchIcon SettingsIcon ShareIcon StarBorderIcon StarIcon SubdirectoryArrowRightIcon
     TuneIcon LogoutIcon UnfoldLessIcon UnfoldMoreIcon TableChartIcon UpdateIcon
     SummarizeIcon TroubleshootIcon InsightsIcon HandymanIcon]]))

(defcard-rg :account-balance-icon "## Account Balance Icon" [AccountBalanceIcon])

(defcard-rg :account-balance-wallet-icon "## Account Balance Wallet Icon" [AccountBalanceWalletIcon])

(defcard-rg :account-circle-icon "## Account Circle Icon" [AccountCircleIcon])

(defcard-rg :add-icon "## Add Icon" [AddIcon])

(defcard-rg :admin-panel-settings-icon "## Admin Panel Settings Icon" [AdminPanelSettingsIcon])

(defcard-rg :all-colapse-icon "## All Colapse Icon" [AllColapseIcon])

(defcard-rg :all-expand-icon "## All Expand Icon" [AllExpandIcon])

(defcard-rg :arrow-downward-icon "## Arrow Downward Icon" [ArrowDownwardIcon])

(defcard-rg :arrow-drop-down-circle-icon "## Arrow Drop Down Circle Icon"
  (apply-stiles [ArrowDropDownCircleIcon {:color "secondary"}]))

(defcard-rg :arrow-drop-down-icon "## Arrow Drop Down Icon" [ArrowDropDownIcon])

(defcard-rg :arrow-drop-up-icon "## Arrow Drop Up Icon" [ArrowDropUpIcon])

(defcard-rg :arrow-left-icon "## Arrow Left Icon" [ArrowLeftIcon])

(defcard-rg :arrow-right-icon "## Arrow Right Icon" [ArrowRightIcon])

(defcard-rg :arrow-upward-icon "## Arrow Upward Icon" [ArrowUpwardIcon])

(defcard-rg :assignment-ind-icon "## Assignment Ind Icon" [AssignmentIndIcon])

(defcard-rg :block-icon "## Block Icon" [BlockIcon])

(defcard-rg :business-icon "## Business Icon" [BusinessIcon])

(defcard-rg :call-made-icon "## Cached Icon" [CachedIcon])

(defcard-rg :calculate-icon "## Calculate Icon" [CalculateIcon])

(defcard-rg :call-made-icon "## Call Made Icon" [CallMadeIcon])

(defcard-rg :chat-bubble-outline-icon "## Chat Bubble Outline Icon" [ChatBubbleOutlineIcon])

(defcard-rg :chat-icon "## Chat Icon" [ChatIcon])

(defcard-rg :check-circle-outline-outlined-icon "## Check Circle Outline Outlined Icon"
  [CheckCircleOutlineOutlinedIcon])

(defcard-rg :chevron-right-icon "## Chevron Right Icon" [ChevronRightIcon])

(defcard-rg :chrome-reader-mode-icon "## Chrome Reader Mode Icon" [ChromeReaderModeIcon])

(defcard-rg :clear-icon "## Clear Icon" [ClearIcon])

(defcard-rg :close-icon "## Close Icon" [CloseIcon])

(defcard-rg :content-copy-icon "## Content Copy Icon" [ContentCopyIcon])

(defcard-rg :delete-icon "## Delete Icon" [DeleteIcon])

(defcard-rg :dialpad-icon "## Dialpad Icon" [DialpadIcon])

(defcard-rg :directions-run-icon "## Directions Run Icon" [DirectionsRunIcon])

(defcard-rg :download-icon "## Download Icon" [DownloadIcon])

(defcard-rg :do-not-touch-icon "## DoNotTouch Icon" [DoNotTouchIcon])

(defcard-rg :edit-icon "## Edit Icon" [EditIcon])

(defcard-rg :emoji-people-icon "## Emoji People Icon" [EmojiPeopleIcon])

(defcard-rg :equalizer-icon "## Equalizer Icon" [EqualizerIcon])

(defcard-rg :error-outline-icon "## Error Outline Icon" [ErrorOutlineIcon])

(defcard-rg :event-icon "## Event Icon" [EventIcon])

(defcard-rg :expand-less-icon "## Expand Less Icon" [ExpandLessIcon])

(defcard-rg :expand-more-icon "## Expand More Icon" [ExpandMoreIcon])

(defcard-rg :exposure-icon "## Exposure Icon" [ExposureIcon])

(defcard-rg :find-in-page-icon "## Find In Page Icon" [FindInPageIcon])

(defcard-rg :format-align-left-outlined-icon "## Format Align Left Outlined Icon" [FormatAlignLeftOutlinedIcon])

(defcard-rg :forward-icon "## Forward Icon" [ForwardIcon])

(defcard-rg :get-app-icon-icon "## GetApp Icon" [GetAppIcon])

(defcard-rg :help-outline-icon "## Help Outline Icon" [HelpOutlineIcon])

(defcard-rg :hierarchy-icon-small "## Hierarchy Icon" [HierarchyIcon {:style {:height "15px" :width "15px"}}])

(defcard-rg :hierarchy-icon "## Hierarchy Icon" [HierarchyIcon {:style {:width "18px"}}])

(defcard-rg :home-outlined-icon "## Home Outlined Icon" [HomeOutlinedIcon])

(defcard-rg :home-icon "## Home Icon" [HomeIcon])

(defcard-rg :info-icon "## Info Icon" [InfoIcon])

(defcard-rg :info-outlined-icon "## Info Outlined Icon" [InfoOutlinedIcon])

(defcard-rg :keyboard-tab-icon "## Keyboard Tab Icon" [KeyboardTabIcon])

(defcard-rg :launch-icon "## Launch Icon" [LaunchIcon])

(defcard-rg :link-icon "## Link Icon" [LinkIcon])

(defcard-rg :link-off-icon "## Link Off Icon" [LinkOffIcon])

(defcard-rg :logo-icon "## Logo Icon" [LogoIcon])

(defcard-rg :logout-icon "## Logout Icon" [LogoutIcon])

(defcard-rg :manage-search-icon "## Manage Search Icon" [ManageSearchIcon])

(defcard-rg :markunread-mailbox-icon "## Markunread Mailbox Icon" [MarkunreadMailboxIcon])

(defcard-rg :menu-icon "## Menu Icon" [MenuIcon])

(defcard-rg :monetization-on-icon "## Monetization On Icon" [MonetizationOnIcon])

(defcard-rg :more-horiz-icon "## More Horiz Icon" [MoreHorizIcon])

(defcard-rg :more-vert-icon "## More Vert Icon" [MoreVertIcon])

(defcard-rg :note-add-icon "## Note Add Icon" [NoteAddIcon])

(defcard-rg :note-outlined-icon "## Note Outlined Icon" [NoteOutlinedIcon])

(defcard-rg :notifications-none-icon "## Notifications None Icon" [NotificationsNoneIcon])

(defcard-rg :people-icon "## People Icon" [PeopleIcon])

(defcard-rg :person-icon "## Person Icon" [PersonIcon])

(defcard-rg :person-search-icon "## Person Search Icon" [PersonSearchIcon])

(defcard-rg :picture-as-pdf-icon "## PictureAsPdf Icon" [PictureAsPdfIcon])

(defcard-rg :playlist-add-icon "## Playlist Add Icon" [PlaylistAddIcon])

(defcard-rg :playlist-add-check-icon "## Playlist Add Check Icon" [PlaylistAddCheckIcon])

(defcard-rg :publish-icon "## Publish Icon" [PublishIcon])

(defcard-rg :refresh-icon "## Refresh Icon" [RefreshIcon])

(defcard-rg :remove-icon "## Remove Icon" [RemoveIcon])

(defcard-rg :report-problem-icon "## Report Problem Icon" [ReportProblemIcon])

(defcard-rg :save-icon "## Save Icon" [SaveIcon])

(defcard-rg :search-icon "## Search Icon" [SearchIcon])

(defcard-rg :settings-icon "## Settings Icon" [SettingsIcon])

(defcard-rg :share-icon "## Share Icon" [ShareIcon])

(defcard-rg :subdirectory-arrow-right-icon "## Subdirectory Arrow Right Icon" [SubdirectoryArrowRightIcon])

(defcard-rg :star-border-icon "## Star Border Icon" [StarBorderIcon])

(defcard-rg :star-icon "## Star Icon" [StarIcon])

(defcard-rg :table-chart-icon "## Table Chart Icon" [TableChartIcon])

(defcard-rg :tune-icon "## Tune Icon" [TuneIcon])

(defcard-rg :unfold-more-icon "## Unfold More Icon" [UnfoldMoreIcon])

(defcard-rg :unfold-less-icon "## Unfold Less Icon" [UnfoldLessIcon])

(defcard-rg :update-icon "## Update Icon" [UpdateIcon])

(defcard-rg :upload-icon "## Upload Icon" [UploadIcon])

(defcard-rg :summarize-icon "## Summarize Icon" [SummarizeIcon])

(defcard-rg :troubleshoot-icon "## Troubleshoot Icon" [TroubleshootIcon])

(defcard-rg :insights-icon "## Insights Icon" [InsightsIcon])

(defcard-rg :handyman-icon "## HandymanIcon" [HandymanIcon])
