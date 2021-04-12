---
id: ui
title: System UI Design
slug: /ui
---

## Login

### Main

import PlantUML from '@theme/PlantUML';

<PlantUML alt="Login Main" src={`
@startsalt
{
  <b>Wonderland DoorKnob System
  {
    Identifier | { ^EMail^ | "username " }
    Password   | { "**** " | Forget password }
    [Login] | Or Register
  }
  --
  Or login via:
  { [GitHub] | [Facebook] | [QQ] | [WeChat] }
  { [EMail] | [SMS] }
}
@endsalt
`} />

### Via Specific Ways

<PlantUML alt="Login Via Specific Ways" src={`
@startsalt
{
  <b>Login via:
  <Specific way name> | "Identifier..."
}
@endsalt
`} />

## Register

### Directly

<PlantUML alt="Register Directly" src={`
@startsalt
{
  <b>Register via:
  { [GitHub] | [Facebook] | [QQ] | [WeChat] }
  --
  <i>Or manually:
  {
    E-Mail*   | "Your E-Mail address    "
    Nickname* | "Name others to call you"
    Password* | "Password            "
    Re-input Password* | "Re-input the password"
  }
  { [Submit] | Login by the existing user }
}
@endsalt
`} />

### Redirect from Social Ways

<PlantUML alt="Register Redirect from Social Ways" src={`
@startsalt
{
  <b>Register via <specific way name> with User <the social display name>:
  {
    Nickname* | "<The social display name>"
    Password* | "Password            "
    Re-input Password* | "Re-input the password"
  }
  { [Submit] | Login by the existing user }
}
@endsalt
`} />

## Users

### Root

<PlantUML alt="Users Root" src={`
@startsalt
{+
  {* <b>DoorKnob | Users | Clients | <i><Nickname: Some Admin> }
  { Users | "Search..." }
  ---
  { [Add] | [Delete Selected] }
  {#
    <b>Id | <b>Nickname | <b>Role | <b>Activated | <b>createdAt        | <b>Actions
    id1   | nickname1   | User    | true         | 2020-01-01 00:00:00 |{ [Delete] }
    id2   | nickname2   | Admin   | true         | 2020-01-01 00:00:00 |{ [Delete] }
    id3   | nickname3   | Owner   | true         | 2020-01-01 00:00:00 | .
  }
}
@endsalt
`} />

### Detail

#### Read

<PlantUML alt="Users Detail Read" src={`
@startsalt
{+
  {* <b>DoorKnob | Users | Clients | <i><Nickname: Some Admin> }
  { Users > <i>id }
  ---
  {
    {<b>Basic Info
    ==}
    { [Edit] | [Delete] }
    Id | <user-id>
    Nickname | <nickname>
    Password | [Update]
    Identifiers
    {#
      <b>Type | <b>Value | <b>Activated
      EMAIL | 1@email.com | true
      PHONE | 185 0000 0000 | { false | [Activate] }
      GITHUB | <i>the-github-id | true
    }
    Role | ADMIN
    Create At | 2020-01-01 00:00:00
    Last Updated At | 2020-01-01 00:00:00
    Service Enabled | { DoorKnob | Absolem }
    Deleted | false
  }
}
@endsalt
`} />

#### Edit

<PlantUML alt="Users Detail Edit" src={`
@startsalt
{+
  {* <b>DoorKnob | Users | Clients | <i><Nickname: Some Admin> }
  { Users > <i>id</i> > Edit }
  ---
  {
    Nickname | "<nickname>"
    Identifiers
    {#
      <b>Type | <b>Value | <b>Actions
      EMAIL | "1@email.com" | [Delete]
      PHONE | "185 0000 0000" | [Delete]
      GITHUB | "<i>the-github-id" | [Delete]
      [Add] | * | *
    }
    Role | ^ADMIN^
    Service Enabled | "DoorKnob Absolem"
  }
  {[Save] | [Cancel]}
}
@endsalt
`} />

## Clients

### Root

<PlantUML alt="Clients Root" src={`
@startsalt
{+
  {* <b>DoorKnob | Users | Clients | <i><Nickname: Some Admin> }
  { Clients | "Search..." }
  ---
  { [Add] | [Delete Selected] }
  {#
    <b>Id | <b>Name | <b>Type | <b>Scopes | <b>createdAt        | <b>Actions
    id1   | nickname1 | Frontend | scope1 scope2 | 2020-01-01 00:00:00 |{ [Delete] }
    id2   | nickname2 | Frontend |scope1 scope2 | 2020-01-01 00:00:00 |{ [Delete] }
    id3   | nickname3 | Backend |scope1 scope2 | 2020-01-01 00:00:00 |{ [Delete] }
  }
}
@endsalt
`} />

### Detail

#### Read

<PlantUML alt="Clients Detail Read" src={`
@startsalt
{+
  {* <b>DoorKnob | Users | Clients | <i><Nickname: Some Admin> }
  { Clients > <i>id }
  ---
  { [Edit] | [Delete] }
  {
    {<b>Basic Info
    ==}
    Id | <user-id>
    Name | <client-name>
    Scopes | scope1 scope2
    Create At | 2020-01-01 00:00:00
    Last Updated At | 2020-01-01 00:00:00
    Type | Frontend
    Redirect URLs | <redirect-url>
    Meta | SkipConsnet
    Deleted | false
  }
}
@endsalt
`} />

#### Edit

<PlantUML alt="Clients Detail Edit" src={`
@startsalt
{+
  {* <b>DoorKnob | Users | Clients | <i><Nickname: Some Admin> }
  { Clients > <i>id</i> > Edit }
  ---
  {
    Name | "<client-name>"
    Scopes | "scope1 scope2"
    Type | ^Frontend^
    Redirect URLs | "<redirect-url>"
    Meta | "SkipConsent"
  }
  {[Save] | [Cancel]}
}
@endsalt
`} />

## \[Incubating] Services

### Root

<PlantUML alt="Services Root" src={`
@startsalt
{+
  {* <b>DoorKnob | Users | Clients | <i><Nickname: Some Admin> }
  { Services | "Search..." }
  ---
  { [Add] | [Delete Selected] }
  {#
    <b>Id | <b>Name | <b>Activated | <b>createdAt        | <b>Actions
    id1   | DoorKnob | true         | 2020-01-01 00:00:00 | { [Delete] }
    id2   | Absolem  | true         | 2020-01-01 00:00:00 | { [Delete] }
  }
}
@endsalt
`} />
