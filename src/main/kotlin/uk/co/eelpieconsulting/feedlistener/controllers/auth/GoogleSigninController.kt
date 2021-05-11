package uk.co.eelpieconsulting.feedlistener.controllers.auth

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Strings
import com.squareup.okhttp.*
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.eelpieconsulting.feedlistener.controllers.ChannelsController
import uk.co.eelpieconsulting.feedlistener.controllers.CurrentUserService
import uk.co.eelpieconsulting.feedlistener.daos.UsersDAO
import java.io.IOException
import javax.servlet.http.HttpServletRequest

@Controller
class GoogleSigninController @Autowired constructor(
        @Value("\${googleAuthClientId}") private val googleClientId: String,
        @Value("\${googleAuthClientSecret}") private val googleClientSecret: String,
        @Value("\${googleAuthCallbackUrl}") private val callbackUrl: String,
        private val currentUserService: CurrentUserService,
        private val usersDAO: UsersDAO
        ) {

    // Does OAuth dance with Google and attaches a Google user to the local session

    private val log = LogManager.getLogger(ChannelsController::class.java)

    private val ALLOWED_EMAIL_DOMAINS = "eelpieconsulting.co.uk"

    private val client = OkHttpClient()
    private val objectMapper = ObjectMapper()
    @RequestMapping(value = ["/signin/redirect"], method = [RequestMethod.GET])
    fun redirect(): ModelAndView {
        // Build a Google auth URL and redirect to it.
        val redirectUrl = buildRedirectUrl()
        log.info("Redirecting to Google login dialog: $redirectUrl")
        return ModelAndView(RedirectView(redirectUrl.toString()))
    }

    @RequestMapping(value = ["/signin/callback"], method = [RequestMethod.GET])
    @Throws(IOException::class, JsonMappingException::class)
    fun callback(@RequestParam(required = false) code: String?, @RequestParam(required = false) error: String?, request: HttpServletRequest): ModelAndView {
        log.info("Received Google auth callback")
        if (Strings.isNullOrEmpty(code)) {
            log.warn("Not code parameter seen on callback. error parameter was: $error")
            return redirectToSigninPrompt()
        }

        log.info("Received code from oauth callback: $code")
        // Post back to Google to get token
        val tokenUrl = HttpUrl.Builder().scheme("https").host("www.googleapis.com").encodedPath("/oauth2/v4/token").addQueryParameter("code", code).addQueryParameter("client_id", googleClientId).addQueryParameter("client_secret", googleClientSecret).addQueryParameter("redirect_uri", callbackUrl).addQueryParameter("grant_type", "authorization_code").build()
        log.info("Exchanging code for token: $tokenUrl")
        val tokenRequest = Request.Builder().url(tokenUrl).post(RequestBody.create(MediaType.parse("text/plain"), "")).build()
        val call = client.newCall(tokenRequest)
        val response = call.execute()
        val token = objectMapper.readValue(response.body().string(), TokenResponse::class.java)
        log.info("Got Google token: $token")
        val tokenInfo = verifyGoogleAccessToken(token.accessToken)
        val googleUserEmail = tokenInfo?.email
        if (!Strings.isNullOrEmpty(googleUserEmail)) {
            log.info("Google token verified to email: $googleUserEmail")
            if (googleUserEmail!!.endsWith(ALLOWED_EMAIL_DOMAINS)) {

                val currentUser = currentUserService.getCurrentUserUser()
                if (currentUser != null) {
                    // If a current user is a signed in but has no Google id then attach to this user
                    if (currentUser.googleUserId == null) {
                        log.info("Attaching google id to current user: " + currentUser!!.username + " / " + googleUserEmail);
                        val currentUserUser = currentUser!!
                        currentUserUser.googleUserId = tokenInfo.userId;
                        usersDAO.save(currentUserUser)
                    }
                    return redirectToSignedInUserUI()

                } else {
                    // If no user is already signed in we can try to sing in by google id
                    val byGoogleId = usersDAO.getByGoogleId(tokenInfo.userId!!)
                    if (byGoogleId != null) {
                        currentUserService.setSignedInUser(byGoogleId)
                        return redirectToSignedInUserUI()
                    } else {
                        return redirectToSigninPrompt();
                    }
                }
            }
        }

        return redirectToSigninPrompt()
    }

    @Throws(IOException::class, JsonMappingException::class)
    private fun verifyGoogleAccessToken(token: String?): TokenInfo? {
        log.info("Verifying up Google access token: $token")
        val tokenInfoUrl = HttpUrl.Builder().scheme("https").host("www.googleapis.com").encodedPath("/oauth2/v2/tokeninfo").addQueryParameter("access_token", token).build()
        val tokenInfoRequest = Request.Builder().url(tokenInfoUrl).get().build()
        val call = client.newCall(tokenInfoRequest)
        val response = call.execute()
        val tokenInfo = objectMapper.readValue(response.body().string(), TokenInfo::class.java)
        log.info("Got Google token info email: ${tokenInfo.email}")
        log.info("Got Google token info id : ${tokenInfo.userId}")
        return tokenInfo
    }

    private fun redirectToSigninPrompt(): ModelAndView {
        return ModelAndView(RedirectView("/signin"))
    }

    private fun redirectToSignedInUserUI(): ModelAndView {
        return ModelAndView(RedirectView("/"))
    }

    private fun buildRedirectUrl(): HttpUrl {
        return HttpUrl.Builder().scheme("https").host("accounts.google.com").encodedPath("/o/oauth2/v2/auth").addQueryParameter("response_type", "code").addQueryParameter("client_id", googleClientId).addQueryParameter("scope", "email").addQueryParameter("prompt", "select_account").addQueryParameter("redirect_uri", callbackUrl).build()
    }

}