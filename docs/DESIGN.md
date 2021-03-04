# DoorKnob Design

Wonderland DoorKnob is the Single Sign On System for Wonderland users logging and managing the accounts.

DoorKnob is combined with several services:

* **doorknob-authentication**: An authentication server for token management (signing, revoking, introspection)
* **doorknob-core**: An main logic for managing aggregations (users, clients)
* **doorknob-proto**: An Protobuf spec of DoorKnob
* **doorknob-endpoint-restful**: An JSON:API Endpoint, marked as the resource server
* **doorknob-endpoint-graphql**: An GraphQL Endpoint, marked as the resource server
* **doorknob-endpoint-proto**: An Protobuf Endpoint, marked as the resource server
* **doorknob-frontend**: Providing login, authorize and admin management features
* Ory Hydra server: The real authentication server
* nginx gateway: A nginx gateway for:
  * proxy the GRPC requests
  * expose the endpoints
  * authenticate and extract the X-USER-ID from requests

## Stories

 ```plantuml
 @startuml
 left to right direction

 Admin --|> User
 Owner --|> Admin

 User --> (Login)
 User --> (Registration)
 User --> (Get own BasicInfo)
 note right of (Get own BasicInfo)
 BasicInfo including:
 * General info like: ID, username, password, email, phone number
 * External OpenID linkages, like QQ, Wechat
 * Status of Wonderland services: is Wonderland Service1 is activated
 end note

 User --> (Update General info)
 User --> (Add/Update/Remove External OpenID linkages)
 User --> (Enable/Disable Wonderland services)

 User --> (Get own consent sessions)
 note right of (Get own consent sessions)
 Consent session is
 https://www.ory.sh/hydra/docs/reference/api#lists-all-consent-sessions-of-a-subject
 end note
 User --> (Revoke a owning consent session)

 Admin --> (Get all Users and Admin)
 Admin --> (Get own BasicInfo of any User)
 Admin --> (Disable any User)
 Admin --> (Get consent sessions of any User)

 Owner --> (Get all Users and Admin and Owner)
 Owner --> (Get own BasicInfo of any User or Admin)
 Owner --> (Disable any User or Admin)
 Owner --> (Upgrade/Downgrade any Admin)
 Owner --> (Get consent sessions of any User or Admin)

 @enduml
 ```
