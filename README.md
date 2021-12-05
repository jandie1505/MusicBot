# MusicBot
[![CodeFactor](https://www.codefactor.io/repository/github/jandie1505/musicbot/badge/master)](https://www.codefactor.io/repository/github/jandie1505/musicbot/overview/master)
## Setup
1. Download the bot
2. Create a start.sh file and write the following command into it: `java -jar MusicBot.jar <token> [shardsCount] [publicMode] [verbose] [bowOwner] [spotifyClientId] [spotifyClientSecret]`. Replace the placeholders: `<token>` = The discord bot token. `[shardsCount]` = The count of shards that should be started (Default: 1). `[publicMode]` = If this is set to true, the bot can be added to all servers. If this is set to false, you need to whitelist a guild to add the bot to it (Default = false, Use console command `guild whitelist` for more information). `[verbose]` = If this is set to true the bot will show extended logs in the console. This is not required for normal use (Default: false).
3. Run the start.sh file.
## Commands
##### User Commands
- /play `<song name / link>` - Add a specific song to queue
- /skip - Skipvote a specific song
- /nowplaying - Get the song that is currently playing
- /queue - Show the queue
- /queue `<index>` - Show the queue from a certain index ("Queue pages")
- /search `<song name>` - Search for a specific song and list the result
##### DJ Commands
- /play - Resume the player
- /leave - Disconnect the bot
- /forceskip - Skip a track
- /movetrack `<from>` `<to>` -Move a specific track in queue
- /remove `<index>` - Remove a specific song from queue
- /clear - Clear the queue
- /shuffle - Shuffles the queue
- /volume <0-200> - Change the volume (Only available on amd64 devices)
- /playnow <song name / link> - Plays a specific song immediately
##### Admin Commands
- /mbsettings info - Overview of settings
- /mbsettings djrole add/remove `<role>` - Add/Remove DJ Roles
- /mbsettings djrole clear - Clear DJ Roles
- /mbsettings ephemeral `<true/false>` - Enable/Disable ephemeral (private) messages
- /mbsettings blacklist add/remove `<link>` - Add/remove link to/from blacklist
- /mbsettings blacklist clear/list - Clear/list blacklist
##### Other commands
- /cmd `<command>` - Send a command to the bot console (Owner only!)
- /help - Shows the bot help page (everyone)
## Bot console
The bot console can also be accessed with the `/cmd <command>` command (owner only).
##### Commands
- `help` - List all commands
- `guild` - Guild management
- `player` - Player management
- `blacklist` - Music blacklist management
- `cmdreload <true>` - Reload commands (true = complete reload)
- `shards` - Shards management
- `verbose` - Verbose logging
