---
id: features
title: System Features Design
slug: /features
---

## User Cases

```plantuml
@startuml
  left to right direction
  Admin --|> User
  Owner --|> Admin
  User --> (Login)
    note bottom of (Login)
    * Login follows OAuth2 spec
    * User can login via:
      * Phone
      * EMail
      * External OIDC linkages
    end note
  User --> (Registration)
    note bottom of (Registration)
    Registration steps:
    * When registering, the \`user.activated = false\`
    * User can activate the account via:
      * Phone
      * EMail
    end note
  User --> (Find Back Password)
    note bottom of (Find Back Password)
    Find back password steps:
    * User can find back the password via:
      * Phone
      * EMail
    end note
  User --> (Get own BasicInfo)
    note bottom of (Get own BasicInfo)
    BasicInfo including:
    * General info like: ID, password, email, phone number
    * External OpenID linkages, like QQ, Wechat
    * Status of Wonderland services: is Wonderland Service1 is activated
    end note
  User --> (Update General info)
  User --> (Add/Update/Remove External OpenID linkages)
  User --> (Enable/Disable Wonderland services)
  User --> (Delete self)
  Admin --> (Get all Users and Admin)
  Admin --> (Get BasicInfo of any User)
  Admin --> (Delete any User)
  Admin --> (Get consent sessions of any User)
  Owner --> (Get all Users and Admin and Owner)
  Owner --> (Get BasicInfo of any User or Admin)
  Owner --> (Delete any User or Admin)
  Owner --> (Upgrade any User to Admin)
  Owner --> (Downgrade any Admin to User)
@enduml
```

### User Login

User has multiple ways to login:
* Via identifiers/password:
  * Identifier contains: EMail, Phone Number
* Via Identifier specific ways:
  * Some identifiers have special ways to login. For example:
    * EMail: Server can send an email
    * Phone Number: Server can send a message
* Via social login

#### Via identifiers/password

```plantuml
@startuml
start
:Server receive type, value and password;
:Find all Identifiers where **identifier.type == type && identifier.value == value && identifier.activated == true**;
if (match) then (yes)
  :Get User by identifier.userId;
  if (user.password == digest(password)) then (yes)
    :Login successful;
  else (no)
    :Fail login, error - identifier and password not patch;
  endif
else (no)
  :Fail login, error - identifier not exist;
endif
stop
@enduml
```

##### Sequence Diagram

See [Ory Hydra Login Flow](https://www.ory.sh/hydra/docs/concepts/login)

#### Via identifier specific way

```plantuml
@startuml
start
:Server receive type and identifier value;
:Find all Identifiers where **identifier.type == type && identifier.value == value && identifier.activated == true**;
if (match) then (yes)
  if (identifier.type == EMAIL) then (yes)
    :Follow loginViaEmail();
  elseif (identifier.type == PHONE) then (yes)
    :Follow loginViaPhone();
  endif
else (no)
  :Fail login, error - identifier not exist;
endif
stop
@enduml
```

#### Via social login (Can be considered as some _identifier specific ways_)

```plantuml
@startuml
start
:Client call **/oauth2/authorization/<provider>**;
:Server call provider's authentication endpoint with **Scope[get-user-info]**;
:Redirect to provider's login page;
:User finishes the login step and redirect to server's callback with access token;
:Using access token, user get profile from the endpoint;
  if (Check if the provider-userId pair exists) then (yes)
    :Just like login via identifier specific way (type=provider-name, value=provider-specific-id);
  else (no)
    :Start register step;
  endif
stop
@enduml
```

##### Sequence Diagram

```plantuml
@startuml
actor User
User -> LoginService: Initiate OAuth2 Authentication Code Flow "GET /oauth2/auth/(provider-name)"
LoginService -> AuthProvider: Redirect to the auth provider
AuthProvider --> LoginService: Finish the authentication and receive access token
LoginService -> AuthProvider: Get the provider's user id
AuthProvider --> LoginService: Return the provider's user profile
LoginService -> LoginService: Check if the user with the provider-userId pair exists
alt User exists
  LoginService -> User: Redirect to consent page
else User not exist
  LoginService -> User: Redirect to register page
end
@enduml
```

### User Signup

User can signup via all identifier types. Each identifier type has specific ways to enable self.

#### Sequence Diagram

```plantuml
@startuml
== Start Create ==
Endpoint -> UserService: UserCommand.StartCreate
UserService -> UserRepository: Check if existing via identifier
UserRepository --> UserService: UserAggregate not exist
UserService -> UserService: Handle command, get new aggregate and UserCreated event
UserService -> UserRepository: Save the new aggregate
UserService -> IdentifierEnabler: Send the activated code via specific ways
UserService -> Endpoint: The new aggregate

== Refrsh Identifier Activate Status ==
Endpoint -> UserService: UserCommand.RefreshIdentifierActivateStatus
UserService -> UserRepository: Get the aggregate via identifier
UserRepository --> UserService: UserAggregate
UserService -> UserService: Handle command, get new aggregate and Refreshed event
UserService -> UserRepository: Save the new aggregate
UserService -> IdentifierEnabler: Send the activated code via specific ways
UserService -> Endpoint: The new aggregate

== Try Finish Create ==
Endpoint -> UserService: UserCommand.TryFinishCreate
UserService -> UserRepository: Get the aggregate via identifier
UserRepository --> UserService: UserAggregate
UserService -> UserService: Handle command, get new aggregate
UserService -> UserRepository: Save the new aggregate
UserService -> Endpoint: The new aggregate
@enduml
```

#### Action Diagram

```plantuml
@startuml
:UserCommand.StartCreate;
if (User with identifier found?) then (yes)
  #pink:Error.IdentifierAlreadyExist;
  kill
endif
:Start Create User;
:UserCommand.RefreshIdentifierActivateStatus;
if (User not found?) then (yes)
  #pink:Error.NotFound;
  kill
elseif (Activate status is refreshable?) then (yes)
  #pink:Error.ActivateStatusNotRefreshableYet;
  kill
endif
:Refresh Activate Status;
:UserCommand.TryFinishCreate;
if (User not found?) then (yes)
  #pink:Error.NotFound;
  kill
elseif (Activate status is expired?) then (yes)
  #pink:Error.ActivateStatusExpired;
  kill
elseif (Activate code not match?) then (yes)
  #pink:Error.ActivateStatusCodeNotMatch;
  kill
endif
:Finish Create User;
@enduml
```

### User Update Info

User can update the user info to self or other users

#### Sequence Diagram

```plantuml
@startuml
Endpoint -> UserService: UserCommand.Update, token
UserService -> Introspector: Introspect token to AuthUser
Introspector --> UserService: AuthUser
UserService -> UserRepository: Get by id
UserRepository --> UserService: UserAggregate
UserService -> UserService: Handle command, get new aggregate
UserService -> UserRepository: Save the new aggregate
UserService -> Endpoint: The new aggregate
@enduml
```

## API Interface

* GET /oauth2/auth: Start login via Ory Hydra
  * Map to `hydra:admin-port/auth2/auth`
* GET /oauth2/auth/(provider-name): Start login via OAuth2 providers
* GET /oauth2/auth/(provider-name)/callback: OAuth2 provider callback
* GET /oauth2/login: Prepare login page
* POST /oauth2/login: Check identifier and credentials
* GET /oauth2/consent: Prepare consent page
* POST /oauth2/consent: Check consent

## Class Diagrams

```plantuml
@startuml
package models {
  Identifier --* User
  ExternalLinkage --* User
  WonderlandServiceType --* User
  Role --* User
  class User {
    id: String
    nickname: String
    password: String
    createdAt: Instant
    lastUpdatedAt: Instant
    identifiers: Map<IdentifierType, Identifier>
    role: Role
    servicesEnabled: Set<WonderlandServiceType>
    activated: Boolean = identifiers.all { it.activated }
    deleted: Boolean
  }
  IdentifierType --* Identifier
  class Identifier {
    type: IdentifierType
    value: String
    activated: Boolean = false
  }
  enum IdentifierType {
    EMAIL, PHONE, GITHUB;
  }
  enum Role {
    USER, ADMIN, OWNER;
  }
  enum WonderlandServiceType {
    DOORKNOB, ABSOLEM;
  }
}
@enduml
```

## References

* https://medium.com/javarevisited/jwt-and-social-authentication-using-spring-boot-90e4faaa9204
* https://github.com/callicoder/spring-boot-react-oauth2-social-login-demo