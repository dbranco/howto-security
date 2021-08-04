# Goal of the project

The project is a use case to enlight a problem that I have questioned in [stackoverflow](https://stackoverflow.com/questions/68628574/spring-security-reactive-mixing-httpbasic-oauth2)

The application, to summarize, tries to configure the Spring Security + Webflux in order to support some resources permiting full access, other resources with Basic Auth and finally other Resources using OAuth2.

The code uses Okta, as the IDP, also includes Okta spring starter in order to autoconfigure the whole access required and localhost callbacks to complete the whole OAuth2 process with Okta.

The idea is to expose 3 GET resources, answering "Dummy call from ${the nature of the call}", as follow:
- /permitall full access no restriction
- /securebasic endpoint secured using HttpBasic Auth, also it is configured in order to the Browser pops the default login form
- /secureoauth endpoint secured using OAuth2.0 supported by Okta, our endpoint will redirect us to Okta Login page and will redirect us back to the resource

The suggestion in stackoverflow needed to have some points in consideration:
1. The ServerHttpSecurity should be split into several ordered Beans
> We can also applies the configuration in several different config classes, but we manage to do it in 1 class with different Beans
2. We have to understand that we are configuring the **Authentication** AND **Authorization** policies
> That means that the Security check if you are authenticated or if contains the expected Authorization Header
> but then will also check if you can access the resource even though
3. So taking into account the previous point you need to understand then that there are 2 methods in the ServerHttpSecurity builder that helps us to indicates whether the Authentication should be done using that rule and if you hace access to it
    1. The **securityMatcher** will tell the Exchange chain if the authentication should be applied using this method
    1. After the **authorizeExchange** you decide the access strategy and to which resources will apply
    
