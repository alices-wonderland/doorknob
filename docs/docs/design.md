---
id: design
title: System Design
slug: /design
---

## User Cases

import PlantUML from '@theme/PlantUML';

<PlantUML alt="User Cases" src={`
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
    * General info like: ID, username, password, email, phone number
    * External OpenID linkages, like QQ, Wechat
    * Status of Wonderland services: is Wonderland Service1 is activated
    end note
  User --> (Update General info)
  User --> (Add/Update/Remove External OpenID linkages)
  User --> (Enable/Disable Wonderland services)
  User --> (Disable self)
  Admin --> (Get all Users and Admin)
  Admin --> (Get BasicInfo of any User)
  Admin --> (Disable any User)
  Admin --> (Get consent sessions of any User)
  Owner --> (Get all Users and Admin and Owner)
  Owner --> (Get BasicInfo of any User or Admin)
  Owner --> (Disable any User or Admin)
  Owner --> (Upgrade any User to Admin)
  Owner --> (Downgrade any Admin to User)
@enduml
`} />

### User Login

User has multiple ways to login:
* Via identifiers/password:
  * Identifier contains: EMail, Phone Number
* Via Identifier specific ways:
  * Some identifiers have special ways to login. For example:
    * EMail: Server can send an email
    * Phone Number: Server can send a message
* Via external OIDC linkages

#### Via identifiers/password

<PlantUML alt='Via identifiers/password' src={`
@startuml
start
:Server receive type, value and password;
:Find all Identifiers where **identifier.type == type && identifier.value == value && identifier.activated == true**;
if (match) then (yes)
  :Get User by identifier.userId;
  if (user.passwordDigest == digest(password)) then (yes)
    :Login successful;
  else (no)
    :Fail login, error - identifier and password not patch;
  endif
else (no)
  :Fail login, error - identifier not exist;
endif
stop
@enduml
`} />

#### Via identifier specific way

<PlantUML alt='Via identifier specific way' src={`
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
`} />

#### Via third-party

<PlantUML alt='Via third-party' src={`
@startuml
start
:Client call **/oauth2/authorization/<provider>**;
:Server call provider's authentication endpoint with **Scope[get-user-info]**;
:Redirect to provider's login page;
:User finishes the login step and redirect to server's callback with access token;
:Using access token, user get profile from the endpoint;
  if (Check if the provider-userId pair exists) then (yes)
    :Continue the consent step;
  else (no)
    :Start register step;
  endif
stop
@enduml
`} />

## Class Diagrams

<PlantUML alt='Class Diagram' src={`
@startuml
  Identifier --* User
  ExternalLinkage --* User
  WonderlandServiceType --* User
  Role --* User
  class User {
    id: String
    nickname: String
    passwordDigest: String
    identifiers: Map<IdentifierType, Identifier>
    role: Role
    externalLinkages: Map<ExternalLinkageType, ExternalLinkage>
    servicesEnabled: Set<WonderlandServiceType>
    activated: Boolean = identifiers.all { it.activated }
  }
  IdentifierType --* Identifier
  class Identifier {
    type: IdentifierType
    value: String
    userId: String
    activated: Boolean = false
  }
  enum IdentifierType {
    EMAIL, PHONE;
  }
  enum Role {
    USER, ADMIN, OWNER;
  }
  ExternalLinkageType --* ExternalLinkage
  class ExternalLinkage {
    type: ExternalLinkageType
    id: String
  }
  enum ExternalLinkageType {
    GITHUB, WECHAT, QQ, GOOGLE...
  }
  enum WonderlandServiceType {
    DOORKNOB, ABSOLEM;
  }
@enduml
`} />
