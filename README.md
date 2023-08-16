# MusicBot
[![CodeFactor](https://www.codefactor.io/repository/github/jandie1505/musicbot/badge/master)](https://www.codefactor.io/repository/github/jandie1505/musicbot/overview/master)
## Setup
1. Download the bot
2. Create a start.sh file and write the following command into it: `[screen -S MusicBot] java -jar MusicBot.jar`. Add the `screen -S MusicBot` part if you want to run MusicBot in a screen.
3. Run the start.sh file.
4. Use the `stop` command to stop the bot.
5. Insert your bot token into the config.json which has been created. If you want to support Spotify playlists you also have to insert a Spotify Client ID and Secret.
6. Start the bot again and it will work.
## Commands
##### User Commands
- /play `<song name / link>` - Add a specific song to queue
- /skip - Skipvote a specific song
- /nowplaying - Get the song that is currently playing
- /queue - Show the queue
- /queue `<index>` - Show the queue from a certain index ("Queue pages")
- /search `<song name>` - Search for a specific song and list the result
##### DJ Commands
- /pause - Pause the player
- /resume - Resume the player
- /stop - Pauses the player if not paused, stops the player and clears queue if already paused
- /connect `[channel]` - Connect the bot to a optionally specified channel
- /disconnect - Disconnect the bot
- /forceskip - Skip a track
- /movetrack `<from>` `<to>` -Move a specific track in queue
- /remove `<index>` - Remove a specific song from queue
- /clear - Clear the queue
- /shuffle - Shuffles the queue
- /volume <0-200> - Change the volume (Only available on amd64 devices)
- /playnow <song name / link> - Plays a specific song immediately
##### Admin Commands
- /mbsettings `<action>` `[value]` - Settings command
- You need to select an action from the dropdown menu of the command.
- All settings are explained if you run the specific action without a value.
##### Other commands
- /cmd `<command>` - Send a command to the bot console (Owner only!)
- /help - Shows the bot help page (everyone)
## Blacklists
There are 3 types of blacklists. The default blacklist or music blacklist, the keyword blacklist and the artist blacklist. The default blacklist is for blacklisting video ids or youtube video urls. The keyword blacklist is for blacklisting keywords in the title of the video. The artist blacklist is for blacklisting channel names. All these blacklists are available as a global and a guild blacklist. Guild admins can bypass the guild blacklist. The global blacklist can't be bypassed. The blacklists can be managed via the `database music-blacklist` command.
## Bot console
The bot console can also be accessed by the configured bot owner with the `/cmd <command>` command.
##### Commands
- `help` - List all commands (planned)
- `bot` - Manage the bot (ShardManager) (start/stop/status)
- `shards` - Shards management (start/stop/list/...)
- `database` - Database management (guild data, guild whitelist, blacklist)
- `guild` - Manage guilds
- `player` - Manage music players
- `commands` - Manage global commands (delete/setup/list)
- `stop` - Shut down MusicBot
