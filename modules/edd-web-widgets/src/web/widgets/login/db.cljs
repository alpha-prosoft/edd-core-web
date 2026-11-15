(ns web.widgets.login.db)

(def remembered-email-key
  "web.widgets.login/last-email")

(def default-db
  {::user                    nil
   ::username                ""
   ::password                ""
   ::confirmation-code       ""
   ::confirmation-visible    false
   ::error-message-visible   false
   ::error-message           ""
   ::form-type               :login
   ::do-after-login          nil
   ::on-logout-hook          nil
   ::forgot-password-visible false
   ::dialog-visible          false
   ::interrupted-event       nil
   ::show-password?          false
   ::loading?                false
   ::resending?              false
   ::notice                  nil
   ::submits                 0
   ::confirmed?              false})
