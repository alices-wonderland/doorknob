---
id: index
title: Introduction
slug: /
---

## What is Wonderland DoorKnob

Wonderland DoorKnob is the Single Sign On System based on OAuth2 Authentication Framework. All Wonderland projects are using it to manage user accounts and register new projects

DoorKnob is combined with several services:

* **doorknob-authentication**: An authentication server for token management (signing, revoking, introspection)
* **doorknob-core**: The business logic for managing aggregations (users, clients)
* **doorknob-proto**: A Protobuf spec of DoorKnob
* **doorknob-endpoint-restful**: An JSON:API Endpoint, marked as the resource server
* **doorknob-endpoint-graphql**: An GraphQL Endpoint, marked as the resource server
* **doorknob-endpoint-proto**: An Protobuf Endpoint, marked as the resource server
* **doorknob-frontend**: The website for DoorKnob
* Ory Hydra server: The real authentication server
* nginx gateway: A nginx gateway for:
  * proxy the GRPC requests
  * expose the endpoints
  * authenticate and extract the X-USER-ID from requests
