(ns web.primitives.icons.core
  (:require
   ["@material-ui/icons/Add" :default Add]
   ["@material-ui/icons/Save" :default Save]
   ["@material-ui/icons/Delete" :default Delete]
   ["@material-ui/icons/Clear" :default Clear]
   ["@material-ui/icons/Search" :default Search]
   ["@material-ui/icons/Refresh" :default Refresh]
   ["@material-ui/icons/Share" :default Share]
   ["@material-ui/icons/Menu" :default Menu]
   ["@material-ui/icons/Tune" :default Tune]
   ["@material-ui/icons/Close" :default Close]
   ["@material-ui/icons/Remove" :default Remove]
   ["@material-ui/icons/UnfoldMore" :default UnfoldMore]
   ["@material-ui/icons/UnfoldLess" :default UnfoldLess]
   ["@material-ui/icons/ExpandMore" :default ExpandMore]
   ["@material-ui/icons/ExpandLess" :default ExpandLess]
   ["@material-ui/icons/MoreHoriz" :default MoreHoriz]
   ["@material-ui/icons/MoreVert" :default MoreVert]
   ["@material-ui/icons/Forward" :default Forward]
   ["@material-ui/icons/SubdirectoryArrowRight" :default SubdirectoryArrowRight]
   ["@material-ui/icons/CallMade" :default CallMade]
   ["@material-ui/icons/Dialpad" :default Dialpad]
   ["@material-ui/icons/Event" :default Event]
   ["@material-ui/icons/ChatBubbleOutline" :default ChatBubbleOutline]
   ["@material-ui/icons/AccountBalance" :default AccountBalance]
   ["@material-ui/icons/AccountBalanceWallet" :default AccountBalanceWallet]
   ["@material-ui/icons/Exposure" :default Exposure]
   ["@material-ui/icons/AccountCircle" :default AccountCircle]
   ["@material-ui/icons/HomeOutlined" :default HomeOutlined]
   ["@material-ui/icons/NoteOutlined" :default NoteOutlined]
   ["@material-ui/icons/FormatAlignLeftOutlined" :default FormatAlignLeftOutlined]
   ["@material-ui/icons/StarBorder" :default StarBorder]
   ["@material-ui/icons/Person" :default Person]
   ["@material-ui/icons/Info" :default Info]
   ["@material-ui/icons/InfoOutlined" :default InfoOutlined]
   ["@material-ui/icons/CheckCircleOutlineOutlined" :default CheckCircleOutlineOutlined]
   ["@material-ui/icons/EmojiPeople" :default EmojiPeople]
   ["@material-ui/icons/DirectionsRun" :default DirectionsRun]
   ["@material-ui/icons/AssignmentInd" :default AssignmentInd]
   ["@material-ui/icons/Equalizer" :default Equalizer]
   ["@material-ui/icons/FindInPage" :default FindInPage]
   ["@material-ui/icons/MarkunreadMailbox" :default MarkunreadMailbox]
   ["@material-ui/icons/NoteAdd" :default NoteAdd]
   ["@material-ui/icons/PlaylistAdd" :default PlaylistAdd]
   ["@material-ui/icons/PlaylistAddCheck" :default PlaylistAddCheck]
   ["@material-ui/icons/Chat" :default Chat]
   ["@material-ui/icons/Star" :default Star]
   ["@material-ui/icons/Link" :default Link]
   ["@material-ui/icons/LinkOff" :default LinkOff]
   ["@material-ui/icons/ErrorOutline" :default ErrorOutline]
   ["@material-ui/icons/HelpOutline" :default HelpOutline]
   ["@material-ui/icons/Publish" :default Publish]
   ["@material-ui/icons/Home" :default Home]
   ["@material-ui/icons/Business" :default Business]
   ["@material-ui/icons/ReportProblemOutlined" :default ReportProblemOutlined]
   ["@material-ui/icons/ChromeReaderMode" :default ChromeReaderMode]
   ["@material-ui/icons/ArrowUpward" :default ArrowUpward]
   ["@material-ui/icons/ArrowDownward" :default ArrowDownward]
   ["@material-ui/icons/KeyboardTab" :default KeyboardTab]
   ["@material-ui/icons/Launch" :default Launch]
   ["@material-ui/icons/NotificationsNone" :default NotificationsNone]
   [web.primitives.icons.utils :refer [handle-props
                                       concat-class-names]]))

(defn AddIcon [props]
  [:> Add props])

(defn SaveIcon [props]
  [:> Save props])

(defn DeleteIcon [props]
  [:> Delete props])

(defn ClearIcon [props]
  [:> Clear props])

(defn SearchIcon [props]
  [:> Search props])

(defn RefreshIcon [props]
  [:> Refresh props])

(defn ShareIcon [props]
  [:> Share props])

(defn MenuIcon [props]
  [:> Menu props])

(defn TuneIcon [props]
  [:> Tune props])

(defn CloseIcon [props]
  [:> Close props])

(defn RemoveIcon [props]
  [:> Remove props])

(defn UnfoldMoreIcon [props]
  [:> UnfoldMore props])

(defn UnfoldLessIcon [props]
  [:> UnfoldLess props])

(defn ExpandMoreIcon [props]
  [:> ExpandMore props])

(defn ExpandLessIcon [props]
  [:> ExpandLess props])

(defn MoreHorizIcon [props]
  [:> MoreHoriz props])

(defn MoreVertIcon [props]
  [:> MoreVert props])

(defn ForwardIcon [props]
  [:> Forward props])

(defn SubdirectoryArrowRightIcon [props]
  [:> SubdirectoryArrowRight props])

(defn CallMadeIcon [props]
  [:> CallMade props])

(defn DialpadIcon [props]
  [:> Dialpad props])

(defn EventIcon [props]
  [:> Event props])

(defn ChatBubbleOutlineIcon [props]
  [:> ChatBubbleOutline props])

(defn ChatIcon [props]
  [:> Chat props])

(defn AccountBalanceIcon [props]
  [:> AccountBalance props])

(defn AccountBalanceWalletIcon [props]
  [:> AccountBalanceWallet props])

(defn ExposureIcon [props]
  [:> Exposure props])

(defn AccountCircleIcon [props]
  [:> AccountCircle props])

(defn HomeOutlinedIcon [props]
  [:> HomeOutlined props])

(defn NoteOutlinedIcon [props]
  [:> NoteOutlined props])

(defn FormatAlignLeftOutlinedIcon [props]
  [:> FormatAlignLeftOutlined props])

(defn StarBorderIcon [props]
  [:> StarBorder props])

(defn CheckCircleOutlineOutlinedIcon [props]
  [:> CheckCircleOutlineOutlined props])

(defn EmojiPeopleIcon [props]
  [:> EmojiPeople props])

(defn DirectionsRunIcon [props]
  [:> DirectionsRun props])

(defn PersonIcon [props]
  [:> Person props])

(defn InfoIcon [props]
  [:> Info props])

(defn InfoOutlinedIcon [props]
  [:> InfoOutlined props])

(defn AssignmentIndIcon [props]
  [:> AssignmentInd props])

(defn EqualizerIcon [props]
  [:> Equalizer props])

(defn FindInPageIcon [props]
  [:> FindInPage props])

(defn MarkunreadMailboxIcon [props]
  [:> MarkunreadMailbox props])

(defn NoteAddIcon [props]
  [:> NoteAdd props])

(defn PlaylistAddIcon [props]
  [:> PlaylistAdd props])

(defn PlaylistAddCheckIcon [props]
  [:> PlaylistAddCheck props])

(defn StarIcon [props]
  [:> Star props])

(defn LinkIcon [props]
  [:> Link props])

(defn LinkOffIcon [props]
  [:> LinkOff props])

(defn ErrorOutlineIcon [props]
  [:> ErrorOutline props])

(defn HelpOutlineIcon [props]
  [:> HelpOutline props])

(defn PublishIcon [props]
  [:> Publish props])

(defn HomeIcon [props]
  [:> Home props])

(defn BusinessIcon [props]
  [:> Business props])

(defn ReportProblemIcon [props]
  [:> ReportProblemOutlined props])

(defn ChromeReaderModeIcon [props]
  [:> ChromeReaderMode props])

(defn ArrowUpwardIcon [props]
  [:> ArrowUpward props])

(defn ArrowDownwardIcon [props]
  [:> ArrowDownward props])

(defn KeyboardTabIcon [props]
  [:> KeyboardTab props])

(defn LaunchIcon [props]
  [:> Launch props])

(defn NotificationsNoneIcon [props]
  [:> NotificationsNone props])

(defn SettingsIcon [props]
  [:svg (handle-props props)
   [:rect {:id "bounding-box-24" :fill "#FFFFFF" :opacity "0" :x "0" :y "0" :width "24" :height "24"}]
   [:path {:d         "M17,13 C19.209139,13 21,14.790861 21,17 C21,19.209139 19.209139,21 17,21 C15.1364348,21 13.570521,19.7256022 13.1262135,18.0007613 L3,18 L3,16 L13.1259548,16.0002435 C13.5699129,14.2748927 15.1360729,13 17,13 Z M17,15 C15.8954305,15 15,15.8954305 15,17 C15,18.1045695 15.8954305,19 17,19 C18.1045695,19 19,18.1045695 19,17 C19,15.8954305 18.1045695,15 17,15 Z M7,3 C8.86392711,3 10.4300871,4.27489272 10.8740452,6.00024347 L21,6 L21,8 L10.8737865,8.00076134 C10.429479,9.72560224 8.86356525,11 7,11 C4.790861,11 3,9.209139 3,7 C3,4.790861 4.790861,3 7,3 Z M7,5 C5.8954305,5 5,5.8954305 5,7 C5,8.1045695 5.8954305,9 7,9 C8.1045695,9 9,8.1045695 9,7 C9,5.8954305 8.1045695,5 7,5 Z"
           :id        "settings"
           :fill      "#2B2D33"
           :fill-rule "nonzero"}]])

(defn EditIcon [props]
  [:svg (handle-props props)
   [:rect {:id "bounding-box-24" :fill "#FFFFFF" :opacity "0" :x "0" :y "0" :width "24" :height "24"}]
   [:path {:d         "M16.7071068,3.29289322 L20.7071068,7.29289322 C21.0976311,7.68341751 21.0976311,8.31658249 20.7071068,8.70710678 L9.70710678,19.7071068 C9.56750624,19.8467073 9.38970726,19.9418625 9.19611614,19.9805807 L4.19611614,20.9805807 C3.496395,21.1205249 2.8794751,20.503605 3.01941932,19.8038839 L4.01941932,14.8038839 C4.05813755,14.6102927 4.15329267,14.4324938 4.29289322,14.2928932 L15.2928932,3.29289322 C15.6834175,2.90236893 16.3165825,2.90236893 16.7071068,3.29289322 Z M13.499444,7.914 L5.92120149,15.4930121 L5.27475488,18.7252451 L8.50698793,18.0787985 L16.085444,10.5 L13.499444,7.914 Z M16,5.41421356 L14.914444,6.5 L17.499444,9.085 L18.5857864,8 L16,5.41421356 Z"
           :id        "edit"
           :fill      "#2B2D33"
           :fill-rule "nonzero"}]])

(defn ArrowLeftIcon [props]
  [:svg (handle-props props)
   [:rect {:id "bounding-box-24" :fill "#FFFFFF" :opacity "0" :x "0" :y "0" :width "24" :height "24"}]
   [:path {:d         "M12,9.41421356 L4.70710678,16.7071068 L3.29289322,15.2928932 L11.2928932,7.29289322 C11.6834175,6.90236893 12.3165825,6.90236893 12.7071068,7.29289322 L20.7071068,15.2928932 L19.2928932,16.7071068 L12,9.41421356 Z"
           :id        "arrow-left"
           :fill      "#2B2D33"
           :fill-rule "nonzero"
           :transform "translate(12.000000, 11.853553) rotate(-90.000000) translate(-12.000000, -11.853553) "}]])

(defn ArrowRightIcon [props]
  [:svg (handle-props props)
   [:rect {:id "bounding-box-24" :fill "#FFFFFF" :opacity "0" :x "0" :y "0" :width "24" :height "24"}]
   [:path {:d         "M12,9.41421356 L4.70710678,16.7071068 L3.29289322,15.2928932 L11.2928932,7.29289322 C11.6834175,6.90236893 12.3165825,6.90236893 12.7071068,7.29289322 L20.7071068,15.2928932 L19.2928932,16.7071068 L12,9.41421356 Z"
           :id        "arrow-right"
           :fill      "#2B2D33"
           :fill-rule "nonzero"
           :transform "translate(12.000000, 11.853553) rotate(-270.000000) translate(-12.000000, -11.853553) "}]])

(defn AllColapseIcon [props]
  [:svg (handle-props props)
   [:path {:d         "M11.59 18.83L14.76 22 16.17 20.59 11.59 16 7 20.59 8.42 22zM11.59 5.17L8.42 2 7.01 3.41 11.59 8 16.18 3.41 14.76 2z"
           :fill-rule "nonzero"}]
   [:path {:d            "M4 11H20V13H4z"
           :stroke       "#979797"
           :stroke-width ".68"}]])

(defn AllExpandIcon [props]
  [:svg (handle-props props)
   [:path {:d         "M11.59 4.83L14.76 8 16.17 6.59 11.59 2 7 6.59 8.42 8zM11.59 19.17L8.42 16 7.01 17.41 11.59 22 16.18 17.41 14.76 16z"
           :fill-rule "nonzero"}]
   [:path {:d            "M4 11H20V13H4z"
           :stroke       "#979797"
           :stroke-width ".68"}]])

(defn LogoIcon [props]
  [:svg (merge {:style {:width   "36px"
                        :height  "34px"
                        :viewbox "0 0 36 34"
                        :version "1.1"
                        :xlmns   "http://www.w3.org/2000/svg"}} props)
   [:path {:d         "M6.78433054,0.600418777 C5.63610913,0.615852373 4.3496967,1.05886756 3.06370843,2.30362121 C0.236864005,5.03973958 0,8.53618948 0,9.54926002 L1.5460119,7.97534313 L7.49221121,13.8831517 L9.14582085,12.2417815 L9.14582085,8.36320095 L10.8447347,10.0439189 L10.8390824,14.731943 L13.2118987,17.0815755 L1.14960884,29.0489638 L5.91223136,33.7650933 L17.9688587,21.8426739 L30.1444093,33.9 L34.9070323,29.1726282 L22.7201545,17.0815755 L25.098635,14.7488063 L25.098635,10.0439189 L26.7918859,8.36320095 L26.7918859,12.2586448 L28.4511585,13.8831517 L34.3916946,7.97534313 L36,9.54926002 C36,8.531595 35.6951798,5.03973958 32.8683357,2.30362121 C29.7009738,-0.760389773 26.5339931,1.0306571 25.4384178,2.11812456 L19.7074137,7.81232974 L21.1231763,9.22885553 L17.9688587,12.3598248 L14.8032151,9.22885553 L16.2359667,7.81232974 L10.4936366,2.11812456 C9.84698403,1.47244057 8.46251123,0.57784829 6.78434123,0.600418777 L6.78433054,0.600418777 Z"
           :id        "Logo"
           :fill      "#2B2D33"
           :fill-rule "nonzero"}]])

(defn Level1Icon [props]
  [:svg (merge {:style {:width   "8px"
                        :height  "8px"
                        :viewbox "0 0 8 8"
                        :version "1.1"
                        :xlmns   "http://www.w3.org/2000/svg"}} props)
   [:circle {:stroke "none" :stroke-width "1" :fill-rule "evenodd" :id "Badge" :fill "#808185" :cx "4" :cy "4" :r "4"}]])

(defn Level2Icon [props]
  [:svg (merge {:style {:width   "8px"
                        :height  "8px"
                        :viewbox "0 0 8 8"
                        :version "1.1"
                        :xlmns   "http://www.w3.org/2000/svg"}} props)
   [:circle {:stroke "none" :stroke-width "1" :fill-rule "evenodd" :id "Badge" :fill "#AAABAD" :cx "4" :cy "4" :r "4"}]])

(defn PersonSearchIcon [props]
  [:svg (merge {:viewBox "0 0 24 24"}
               (dissoc props :class-name)
               {:class (concat-class-names ["MuiSvgIcon-root" (:class-name props)])})
   [:rect {:fill "none" :height "24" :width "24"}]
   [:circle {:cx "10" :cy "8" :r "4"}]
   [:path {:d "M10.35,14.01C7.62,13.91,2,15.27,2,18v2h9.54C9.07,17.24,10.31,14.11,10.35,14.01z"}]
   [:path {:d "M19.43,18.02C19.79,17.43,20,16.74,20,16c0-2.21-1.79-4-4-4s-4,1.79-4,4c0,2.21,1.79,4,4,4c0.74,0,1.43-0.22,2.02-0.57 L20.59,22L22,20.59L19.43,18.02z M16,18c-1.1,0-2-0.9-2-2c0-1.1,0.9-2,2-2s2,0.9,2,2C18,17.1,17.1,18,16,18z"}]])
