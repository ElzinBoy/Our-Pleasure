# Our-Pleasure Bot

A lightweight Discord NSFW, bot built with Java 25, JDA 6, Spring Boot 4, and PostgreSQL, packaged using Nix (Devenv) for easy development and deployment.

---

## Features
- **/hentai**: displays a random hentai title or a specific title by ID. Includes PC and mobile display modes, interactive page switching, and full metadata embeds.
- **/add_hentai**: allows administrators to add new hentai entries to the database with interactive confirm/cancel buttons.
- **Embedded Database**: local PostgreSQL service managed entirely within the Nix environment.
- **Autostart**: complete Systemd service configuration for remote server deployment.

---

## Local Development Setup

To run the project locally, you will need [Nix](https://nixos.org/) and [Devenv](https://devenv.sh/) installed.

> [!NOTE]
> Devenv requires Nix experimental features (`nix-command` and `flakes`) to be enabled. If they are not enabled, you can enable them by adding `experimental-features = nix-command flakes` to your Nix configuration file (e.g. `~/.config/nix/nix.conf`).

### 1. Configure Environment Variables
Create a `.env` file in the root of the project:
```env
DB_HOST=127.0.0.1
DB_PORT=5430
DB_NAME=pleasure_db
DB_USERNAME=pleasure_user
DB_PASSWORD=pleasure_pass_123
DISCORD_BOT_TOKEN=your_discord_bot_token_here
BOT_COMMANDS_LANG=ru # Set to "en" for English, "ru" for Russian (default)
```
> [!IMPORTANT]
> The `.env` file contains sensitive credentials and is ignored by Git. Do not commit it.

### 2. Launch the Application
Start the database service and bot runner:
```bash
devenv up
```
This command automatically spins up a local PostgreSQL instance on port `5430` inside the `.devenv` directory, builds the project using Maven, and starts the bot application.

---

## Remote Server (VPS) Deployment

### 1. Install Nix and Devenv
On your remote server, run:
```bash
# Install Nix
curl -L https://nixos.org/nix/install | sh
. ~/.nix-profile/etc/profile.d/nix.sh

# Enable experimental Nix features (nix-command and flakes) required for Devenv
mkdir -p ~/.config/nix && echo "experimental-features = nix-command flakes" >> ~/.config/nix/nix.conf

# Install Devenv
nix profile add nixpkgs#devenv
```

### 2. Clone the Project and Configure
```bash
git clone https://github.com/ElzinBoy/Our-Pleasure.git
cd Our-Pleasure

# Create .env with your production bot token
nano .env
```

### 3. Setup Autostart (Systemd Service)
To ensure the bot and database start automatically upon server restart, configure a Systemd service:

Create the service file:
```bash
sudo nano /etc/systemd/system/ourpleasure.service
```

Paste the following configuration:
```ini
[Unit]
Description=Our Pleasure Bot (Devenv)
After=network.target

[Service]
Type=simple
User=YOUR_SYSTEM_USERNAME
WorkingDirectory=/path/to/Our-Pleasure
ExecStart=/bin/sh -c 'export PATH=$PATH:$HOME/.nix-profile/bin:/nix/var/nix/profiles/default/bin; devenv up'
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```
*Make sure to replace `YOUR_SYSTEM_USERNAME` and `/path/to/Our-Pleasure` with your server's actual values.*

Enable and start the service:
```bash
sudo systemctl daemon-reload
sudo systemctl enable ourpleasure.service
sudo systemctl start ourpleasure.service
```

### 4. Monitoring the Bot
- **Check Status**: `sudo systemctl status ourpleasure.service`
- **View Live Logs**: `sudo journalctl -u ourpleasure.service -f`
- **Restart manually**: `sudo systemctl restart ourpleasure.service`

---

## Database Backups

To create a backup of your local database on the host machine:
```bash
pg_dump -h 127.0.0.1 -p 5430 -U pleasure_user -d pleasure_db -F c -b -v -f our_pleasure_backup.dump
```

To restore the backup:
```bash
pg_restore -h 127.0.0.1 -p 5430 -U pleasure_user -d pleasure_db -v our_pleasure_backup.dump
```
