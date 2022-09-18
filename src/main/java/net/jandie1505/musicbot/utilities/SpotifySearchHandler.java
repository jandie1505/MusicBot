package net.jandie1505.musicbot.utilities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.util.ArrayList;
import java.util.List;

public class SpotifySearchHandler {
    public static List<AudioTrack> search(String playlistlink, String clientId, String clientSecret) {
        List<AudioTrack> returnList = new ArrayList<>();

        if(((clientSecret != null) && (!clientSecret.equals("")) && (clientId != null ) && (!clientId.equals("")))) {
            try {
                if(playlistlink.startsWith("https://open.spotify.com/playlist/")) {
                    // GET SPOTIFY PLAYLIST ID
                    String playlistEditString = playlistlink.replace("https://open.spotify.com/playlist/", "");
                    String[] playlistEditStringArray = playlistEditString.split("\\?");
                    String playlistId = playlistEditStringArray[0];

                    // CREATE NEW SPOTIFY API
                    SpotifyApi spotifyApi = new SpotifyApi.Builder()
                            .setClientId(clientId)
                            .setClientSecret(clientSecret)
                            .build();

                    // GET SPOTIFY ACCESS TOKEN
                    ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
                    ClientCredentials clientCredentials = clientCredentialsRequest.execute();
                    spotifyApi.setAccessToken(clientCredentials.getAccessToken());

                    // GET PLAYLISTS ITEMS
                    GetPlaylistsItemsRequest playlistsItemsRequest = spotifyApi.getPlaylistsItems(playlistId).build();
                    Paging<PlaylistTrack> playlistTrackPaging = playlistsItemsRequest.execute();

                    // GET TRACKS OF PLAYLIST ENTRIES, SEARCH FOR THEM ON YOUTUBE AND ADD THEM TO A LIST
                    for(PlaylistTrack playlistTrack : playlistTrackPaging.getItems()) {
                        // GET TRACK
                        GetTrackRequest getTrackRequest = spotifyApi.getTrack(playlistTrack.getTrack().getId()).build();
                        Track track = getTrackRequest.execute();

                        String searchString = track.getArtists()[0].getName() + " - " + track.getName();
                        List<AudioTrack> trackList = YTSearchHandler.search(searchString);

                        if(!trackList.isEmpty()) {
                            returnList.add(trackList.get(0));
                        }
                    }
                }
            } catch(Exception ignored) {}
        }

        return returnList;
    }
}