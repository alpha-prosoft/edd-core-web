(ns web.primitives.icons.core
  (:require
   ["@mui/icons-material/AccountBalance" :default AccountBalance]
   ["@mui/icons-material/AccountBalanceWallet" :default AccountBalanceWallet]
   ["@mui/icons-material/AccountCircle" :default AccountCircle]
   ["@mui/icons-material/Add" :default Add]
   ["@mui/icons-material/AdminPanelSettings" :default AdminPanelSettings]
   ["@mui/icons-material/ArrowDownward" :default ArrowDownward]
   ["@mui/icons-material/ArrowDropDown" :default ArrowDropDown]
   ["@mui/icons-material/ArrowDropDownCircle" :default ArrowDropDownCircle]
   ["@mui/icons-material/ArrowDropUp" :default ArrowDropUp]
   ["@mui/icons-material/ArrowUpward" :default ArrowUpward]
   ["@mui/icons-material/AssignmentInd" :default AssignmentInd]
   ["@mui/icons-material/Block" :default Block]
   ["@mui/icons-material/Business" :default Business]
   ["@mui/icons-material/Calculate" :default Calculate]
   ["@mui/icons-material/CallMade" :default CallMade]
   ["@mui/icons-material/Cached" :default Cached]
   ["@mui/icons-material/Chat" :default Chat]
   ["@mui/icons-material/ChatBubbleOutline" :default ChatBubbleOutline]
   ["@mui/icons-material/CheckCircleOutlineOutlined" :default CheckCircleOutlineOutlined]
   ["@mui/icons-material/ChromeReaderMode" :default ChromeReaderMode]
   ["@mui/icons-material/Clear" :default Clear]
   ["@mui/icons-material/Close" :default Close]
   ["@mui/icons-material/ContentCopy" :default ContentCopy]
   ["@mui/icons-material/Delete" :default Delete]
   ["@mui/icons-material/Dialpad" :default Dialpad]
   ["@mui/icons-material/DirectionsRun" :default DirectionsRun]
   ["@mui/icons-material/DoNotTouch" :default DoNotTouch]
   ["@mui/icons-material/Download" :default Download]
   ["@mui/icons-material/Edit" :default Edit]
   ["@mui/icons-material/EmojiPeople" :default EmojiPeople]
   ["@mui/icons-material/ErrorOutline" :default ErrorOutline]
   ["@mui/icons-material/ExpandLess" :default ExpandLess]
   ["@mui/icons-material/ExpandMore" :default ExpandMore]
   ["@mui/icons-material/Exposure" :default Exposure]
   ["@mui/icons-material/Event" :default Event]
   ["@mui/icons-material/Equalizer" :default Equalizer]
   ["@mui/icons-material/FindInPage" :default FindInPage]
   ["@mui/icons-material/FormatAlignLeftOutlined" :default FormatAlignLeftOutlined]
   ["@mui/icons-material/Forward" :default Forward]
   ["@mui/icons-material/GetApp" :default GetApp]
   ["@mui/icons-material/HelpOutline" :default HelpOutline]
   ["@mui/icons-material/Home" :default Home]
   ["@mui/icons-material/HomeOutlined" :default HomeOutlined]
   ["@mui/icons-material/Info" :default Info]
   ["@mui/icons-material/InfoOutlined" :default InfoOutlined]
   ["@mui/icons-material/KeyboardTab" :default KeyboardTab]
   ["@mui/icons-material/Launch" :default Launch]
   ["@mui/icons-material/Link" :default Link]
   ["@mui/icons-material/LinkOff" :default LinkOff]
   ["@mui/icons-material/Logout" :default Logout]
   ["@mui/icons-material/ManageSearch" :default ManageSearch]
   ["@mui/icons-material/MarkunreadMailbox" :default MarkunreadMailbox]
   ["@mui/icons-material/Menu" :default Menu]
   ["@mui/icons-material/MonetizationOn" :default MonetizationOn]
   ["@mui/icons-material/MoreHoriz" :default MoreHoriz]
   ["@mui/icons-material/MoreVert" :default MoreVert]
   ["@mui/icons-material/NoteAdd" :default NoteAdd]
   ["@mui/icons-material/NoteOutlined" :default NoteOutlined]
   ["@mui/icons-material/NotificationsNone" :default NotificationsNone]
   ["@mui/icons-material/People" :default People]
   ["@mui/icons-material/Person" :default Person]
   ["@mui/icons-material/PersonSearch" :default PersonSearch]
   ["@mui/icons-material/PictureAsPdf" :default PictureAsPdf]
   ["@mui/icons-material/PlaylistAdd" :default PlaylistAdd]
   ["@mui/icons-material/PlaylistAddCheck" :default PlaylistAddCheck]
   ["@mui/icons-material/Publish" :default Publish]
   ["@mui/icons-material/Refresh" :default Refresh]
   ["@mui/icons-material/Remove" :default Remove]
   ["@mui/icons-material/ReportProblemOutlined" :default ReportProblemOutlined]
   ["@mui/icons-material/Save" :default Save]
   ["@mui/icons-material/Search" :default Search]
   ["@mui/icons-material/Share" :default Share]
   ["@mui/icons-material/Star" :default Star]
   ["@mui/icons-material/StarBorder" :default StarBorder]
   ["@mui/icons-material/SubdirectoryArrowRight" :default SubdirectoryArrowRight]
   ["@mui/icons-material/Tune" :default Tune]
   ["@mui/icons-material/UnfoldLess" :default UnfoldLess]
   ["@mui/icons-material/UnfoldMore" :default UnfoldMore]
   ["@mui/icons-material/ChevronRight" :default ChevronRight]
   ["@mui/icons-material/TableChart" :default TableChart]
   ["@mui/icons-material/Update" :default Update]
   ["@mui/icons-material/Upload" :default Upload]
   ["@mui/icons-material/Summarize" :default Summarize]
   ["@mui/icons-material/Troubleshoot" :default Troubleshoot]
   ["@mui/icons-material/Insights" :default Insights]
   ["@mui/icons-material/Handyman" :default Handyman]

   [web.primitives.icons.utils :refer [handle-props]]))

(defn AddIcon [props]
  [:> Add props])

(defn DoNotTouchIcon [props]
  [:> DoNotTouch props])

(defn SaveIcon [props]
  [:> Save props])

(defn DeleteIcon [props]
  [:> Delete props])

(defn CalculateIcon [props]
  [:> Calculate props])

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

(defn MonetizationOnIcon [props]
  [:> MonetizationOn props])

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

(defn CachedIcon [props]
  [:> Cached props])

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

(defn PeopleIcon [props]
  [:> People props])

(defn PersonIcon [props]
  [:> Person props])

(defn PictureAsPdfIcon [props]
  [:> PictureAsPdf props])

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

(defn BlockIcon [props]
  [:> Block props])

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

(defn PersonSearchIcon [props]
  [:> PersonSearch props])

(defn AdminPanelSettingsIcon [props]
  [:> AdminPanelSettings props])

(defn ChevronRightIcon [props]
  [:> ChevronRight props])

(defn TableChartIcon [props]
  [:> TableChart props])

(defn UpdateIcon [props]
  [:> Update props])

(defn DownloadIcon [props]
  [:> Download props])

(defn ArrowDropDownCircleIcon [props]
  [:> ArrowDropDownCircle props])

(defn ArrowDropDownIcon [props]
  [:> ArrowDropDown props])

(defn ContentCopyIcon [props]
  [:> ContentCopy  props])

(defn ArrowDropUpCircleIcon [{:keys [style] :as props}]
  [:> ArrowDropDownCircle (assoc props
                                 :style (merge {:rotate "180deg"} style))])

(defn ArrowDropUpIcon [props]
  [:> ArrowDropUp props])

(defn ManageSearchIcon [props]
  [:> ManageSearch props])

(defn GetAppIcon [props]
  [:> GetApp props])

(defn SettingsIcon [props]
  [:svg (handle-props props)
   [:rect {:id "bounding-box-24" :fill "#FFFFFF" :opacity "0" :x "0" :y "0" :width "24" :height "24"}]
   [:path {:d         "M17,13 C19.209139,13 21,14.790861 21,17 C21,19.209139 19.209139,21 17,21 C15.1364348,21 13.570521,19.7256022 13.1262135,18.0007613 L3,18 L3,16 L13.1259548,16.0002435 C13.5699129,14.2748927 15.1360729,13 17,13 Z M17,15 C15.8954305,15 15,15.8954305 15,17 C15,18.1045695 15.8954305,19 17,19 C18.1045695,19 19,18.1045695 19,17 C19,15.8954305 18.1045695,15 17,15 Z M7,3 C8.86392711,3 10.4300871,4.27489272 10.8740452,6.00024347 L21,6 L21,8 L10.8737865,8.00076134 C10.429479,9.72560224 8.86356525,11 7,11 C4.790861,11 3,9.209139 3,7 C3,4.790861 4.790861,3 7,3 Z M7,5 C5.8954305,5 5,5.8954305 5,7 C5,8.1045695 5.8954305,9 7,9 C8.1045695,9 9,8.1045695 9,7 C9,5.8954305 8.1045695,5 7,5 Z"
           :id        "settings"
           :fill      "#2B2D33"
           :fill-rule "nonzero"}]])

(defn SummarizeIcon [props]
  [:> Summarize props])

(defn TroubleshootIcon [props]
  [:> Troubleshoot props])

(defn InsightsIcon [props]
  [:> Insights props])

(defn HandymanIcon [props]
  [:> Handyman props])

(defn EditIcon [props]
  [:> Edit props])

(defn UploadIcon [props]
  [:> Upload props])

(defn LogoutIcon [props]
  [:> Logout props])

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

(defn HierarchyIcon [props]
  [:svg (merge {:viewBox "0 0 14 14"
                :color "red"
                :version "1.1"
                :xlmns   "http://www.w3.org/2000/svg"}
               props)
   [:g {:stroke "none", :stroke-width "1", :fill "none", :fill-rule "evenodd"}
    [:rect {:x "0", :y "0", :width "14", :height "14"}]
    [:g {:fill "#212121"}
     [:rect {:x "0", :y "0", :width "14", :height "3"}]
     [:rect {:x "5", :y "6", :width "9", :height "3"}]
     [:rect {:x "5", :y "11", :width "9", :height "3"}]
     [:rect {:x "0", :y "4", :width "1", :height "3"}]
     [:rect {:x "0", :y "7", :width "1", :height "5"}]
     [:rect {:x "0", :y "7", :width "4", :height "1"}]
     [:rect {:x "0", :y "12", :width "4", :height "1"}]]]])
