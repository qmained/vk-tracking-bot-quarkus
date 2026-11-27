# VK Tracking Bot

A Telegram bot that watches VK users' online status and messages you whenever it changes:
when someone comes online or goes offline, switches device, or gets their page deactivated.

Built with [Quarkus](https://quarkus.io/) and Kotlin, using [KtGram](https://github.com/vendelieu/telegram-bot)
through its Quarkus extension. Runs on the JVM or as a GraalVM native executable.

## Features

- **Per-user watch lists.** Each Telegram user keeps their own list of VK profiles to follow.
- **Status notifications** with name, online state, last-seen time, how long the person was online or offline,
  and platform (Android, iPhone, PC, …).
- **Whitelist.** Only users approved by the admin can use the bot; the admin is notified about every new user.
- **VK rate-limit aware.** On VK's flood-control error the checker pauses for about a minute instead of hammering the API.

## Commands

| Command   | Who       | What it does                                                         |
|-----------|-----------|----------------------------------------------------------------------|
| `/start`  | allowed   | Shows that the bot is alive.                                         |
| `/add`    | allowed   | Asks for a VK numeric id and starts watching it.                     |
| `/delete` | allowed   | Asks for a VK id and stops watching it.                              |
| `/list`   | allowed   | Lists the VK profiles you watch, with links.                         |
| `/allow`  | admin     | Asks for a Telegram user id and adds it to the whitelist.            |
| `/forbid` | admin     | Asks for a Telegram user id and removes it from the whitelist.       |

A VK numeric id can be looked up at <https://regvk.com/id/>.

## How it works

```
Telegram ──long polling──▶ KtGram handlers ──▶ PostgreSQL (users, watch lists, last known status)
                                                   ▲
VK API (users.get) ◀── scheduler, every 4 s ──────┘──▶ Telegram notification on change
```

- `telegram/handlers`, `telegram/controller`: commands and their follow-up inputs, guarded by
  `AllowedUserFilter` / `AdminFilter`.
- `telegram/schedulers/OnlineChecker`: polls VK for all watched profiles and notifies their watchers on changes.
- `service`, `model`: Hibernate ORM with Panache entities and transactional services.
- `clients/VkClient`: Quarkus REST client for `users.get`.

## Requirements

- JDK 21+ (for the native build: GraalVM or Mandrel, or Docker for a container build)
- PostgreSQL (a `docker-compose.yaml` is included)
- A Telegram bot token from [@BotFather](https://t.me/BotFather)
- A VK access token that can call [`users.get`](https://dev.vk.com/method/users.get) with the `online` and `last_seen` fields

> **Note:** the bot depends on `eu.vendeli:quarkus-ktgram`, KtGram's Quarkus extension, which is not released yet.
> Until it is, get it from the `quarkus-extension` branch of [qmained/telegram-bot](https://github.com/qmained/telegram-bot)
> (a fork of KtGram) and publish it to your local Maven repository:
>
> ```shell script
> git clone -b quarkus-extension https://github.com/qmained/telegram-bot.git
> cd telegram-bot
> # only for native builds: also bring in the GraalVM native-image metadata support
> git merge origin/new-runtime-hints
> ./gradlew publishToMavenLocal -PlibVersion=<version>
> ```
>
> The `quarkus-extension` branch alone is enough for the JVM build. The native executable also needs
> `new-runtime-hints`, which ships the metadata GraalVM needs for KtGram itself. Both branches are open as pull requests to KtGram, and this merge step is only needed until they're accepted.
> After that, the bot will use the released KtGram version and none of these steps will be needed.
>
> Then use the same `<version>` for the `eu.vendeli.telegram-bot` plugin and `eu.vendeli:quarkus-ktgram`
> in `build.gradle.kts`, and keep `mavenLocal()` in the repositories of both `settings.gradle.kts` and `build.gradle.kts`.

## Configuration

Secrets are never committed. `application.yaml` refers to them as `${...}` placeholders, and Quarkus reads the
values from a `.env` file in the working directory (or from environment variables, which take precedence).

```shell script
cp .env.example .env
```

| Variable          | Description                                                                                 |
|-------------------|---------------------------------------------------------------------------------------------|
| `BOT_TOKEN`       | Telegram bot token.                                                                         |
| `VK_ACCESS_TOKEN` | VK API access token.                                                                        |
| `ADMIN_ID`        | Telegram user id of the admin (can use `/allow` and `/forbid`, gets new-user notifications). |
| `VK_ADMIN_ID`     | VK id of the admin's own profile; only the admin can add it to a watch list.               |

`.env` is listed in `.gitignore`. Keep it out of the repository, and on a server restrict it to your user
(`chmod 600 .env`).

## Running locally

Start PostgreSQL:

```shell script
docker compose up -d
```

Run in dev mode, with live reload and the Dev UI at <http://localhost:8080/q/dev/>:

```shell script
./gradlew quarkusDev
```

## Building and running

### JVM

```shell script
./gradlew build
java -jar build/quarkus-app/quarkus-run.jar
```

### Native executable

```shell script
./gradlew build -x test -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false
./build/vk-tracking-bot-quarkus-1.0-SNAPSHOT-runner
```

Without a local GraalVM, add `-Dquarkus.native.container-build=true` to build inside a container.

Start the app from the directory that contains `.env`, because that's where Quarkus looks for it.
Under systemd, set `WorkingDirectory=` to that directory or point `EnvironmentFile=` at the file.

## Troubleshooting

- **The bot starts but never answers.** Check that the host can reach Telegram:
  `curl -m 10 -o /dev/null -w "%{http_code}\n" https://api.telegram.org` should print `302`.
  If the connection times out, Telegram is blocked on that network. KtGram has no HTTP timeouts by default,
  so polling then waits silently: route the host through a VPN or proxy.
- **The bot answers nobody.** New users aren't whitelisted. The admin (`ADMIN_ID`) has to `/allow` them first.
- **A dependency built locally isn't picked up on another machine.** Gradle caches non-`SNAPSHOT` versions forever;
  rebuild with `--refresh-dependencies`, or publish local builds with a `-SNAPSHOT` version.

## Tech stack

Kotlin · Quarkus 3.40 · KtGram · Hibernate ORM with Panache · PostgreSQL · Quarkus REST Client · GraalVM native image
