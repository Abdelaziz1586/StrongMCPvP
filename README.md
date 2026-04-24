# StrongMCPvP

A feature-rich PvP plugin for Spigot 1.8.8 Minecraft servers.

## Overview

StrongMCPvP provides a complete PvP experience out of the box — including kits, kill streaks, quests, levels, a shop, leaderboards, scoreboards, combat logging protection, particle trails, and Discord integration.

## Intended Audience

Minecraft server owners who want to quickly set up a competitive PvP server without building everything from scratch.

## Requirements

- Java 8+
- Spigot / CraftBukkit 1.8.8
- MySQL database
- (Optional) [LuckPerms](https://luckperms.net/) — for permissions
- (Optional) [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) — for placeholders

## Getting Started

### 1. Clone and build

```bash
git clone https://github.com/Abdelaziz1586/StrongMCPvP.git
cd StrongMCPvP
mvn clean package
```

The compiled jar will be in the `target/` folder.

### 2. Install

Copy the jar into your server's `plugins/` folder, then start (or restart) the server.

```
cp target/StrongMCPvP-1.0-SNAPSHOT.jar /path/to/your/server/plugins/
```

### 3. Start your server

```
java -jar spigot-1.8.8.jar
```

The plugin will generate a `plugins/StrongMCPvP/` folder with `config.yml` and `data.yml` on first run.

## Configuration

Open `plugins/StrongMCPvP/config.yml` and fill in your MySQL credentials and any settings you want to change, then run `/pvp reload` (or restart the server) to apply your changes.

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes (`git commit -m "Add my feature"`)
4. Push to your fork (`git push origin feature/my-feature`)
5. Open a Pull Request against `master`

## License

This project is licensed under the [MIT License](LICENSE).
