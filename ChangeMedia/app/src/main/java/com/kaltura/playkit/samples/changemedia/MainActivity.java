package com.kaltura.playkit.samples.changemedia;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.kaltura.playkit.PKMediaConfig;
import com.kaltura.playkit.PKMediaEntry;
import com.kaltura.playkit.PKMediaFormat;
import com.kaltura.playkit.PKMediaSource;
import com.kaltura.playkit.PlayKitManager;
import com.kaltura.playkit.Player;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    //The url of the first source to play
    private static final String FIRST_SOURCE_URL = "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8";
    //The url of the second source to play
    private static final String SECOND_SOURCE_URL = "https://cdnapisec.kaltura.com/p/2215841/sp/221584100/playManifest/entryId/1_w9zx2eti/protocol/https/format/url/falvorIds/1_1obpcggb,1_yyuvftfz,1_1xdbzoa6,1_k16ccgto,1_djdf6bk8/a.mp4";

    //id of the first entry
    private static final String FIRST_ENTRY_ID = "entry_id_1";
    //id of the second entry
    private static final String SECOND_ENTRY_ID = "entry_id_2";
    //id of the first media source.
    private static final String FIRST_MEDIA_SOURCE_ID = "source_id_1";
    //id of the second media source.
    private static final String SECOND_MEDIA_SOURCE_ID = "source_id_2";

    private Player player;
    private PKMediaConfig mediaConfig;
    private Button playPauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //First. Create PKMediaConfig object.
        mediaConfig = new PKMediaConfig();

        //Create instance of the player.
        player = PlayKitManager.loadPlayer(this, null);

        //Add player to the view hierarchy.
        addPlayerToView();

        //Add simple play/pause button.
        addPlayPauseButton();

        //Init change media button which will switch between entries.
        initChangeMediaButton();

        //Prepare the first entry.
        prepareFirstEntry();
    }

    /**
     * Initialize the changeMedia button. On click it will change media.
     */
    private void initChangeMediaButton() {
        //Get reference to the button.
        Button changeMediaButton = (Button) this.findViewById(R.id.change_media_button);
        //Set click listener.
        changeMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Change media.
                changeMedia();
            }
        });
    }

    /**
     * Will switch between entries. If the first entry is currently active it will
     * prepare the second one. Otherwise it will prepare the first one.
     */
    private void changeMedia() {

        //Before changing media we must call stop on the player.
        player.stop();

        //Check if id of the media entry that is set in mediaConfig.
        if (mediaConfig.getMediaEntry().getId().equals(FIRST_ENTRY_ID)) {

            //If first one is active, prepare second one.
            prepareSecondEntry();
        } else {
            //If the second one is active, prepare the first one.
            prepareFirstEntry();
        }

        //Just reset the playPauseButton text to "Play".
        resetPlayPauseButtonToPlayText();
    }



    /**
     * Prepare the first entry.
     */
    private void prepareFirstEntry() {
        //Second. Create PKMediaEntry object.
        PKMediaEntry mediaEntry = createFirstMediaEntry();

        //Add it to the mediaConfig.
        mediaConfig.setMediaEntry(mediaEntry);

        //Prepare player with media configuration.
        player.prepare(mediaConfig);
    }

    /**
     * Prepare the second entry.
     */
    private void prepareSecondEntry() {
        //Second. Create PKMediaEntry object.
        PKMediaEntry mediaEntry = createSecondMediaEntry();

        //Add it to the mediaConfig.
        mediaConfig.setMediaEntry(mediaEntry);

        //Prepare player with media configuration.
        player.prepare(mediaConfig);
    }

    /**
     * Create {@link PKMediaEntry} with minimum necessary data.
     *
     * @return - the {@link PKMediaEntry} object.
     */
    private PKMediaEntry createFirstMediaEntry() {
        //Create media entry.
        PKMediaEntry mediaEntry = new PKMediaEntry();

        //Set id for the entry.
        mediaEntry.setId(FIRST_ENTRY_ID);

        //Set media entry type. It could be Live,Vod or Unknown.
        //For now we will use Unknown.
        mediaEntry.setMediaType(PKMediaEntry.MediaEntryType.Unknown);

        //Create list that contains at least 1 media source.
        //Each media entry can contain a couple of different media sources.
        //All of them represent the same content, the difference is in it format.
        //For example same entry can contain PKMediaSource with dash and another
        // PKMediaSource can be with hls. The player will decide by itself which source is
        // preferred for playback.
        List<PKMediaSource> mediaSources = createFirstMediaSources();

        //Set media sources to the entry.
        mediaEntry.setSources(mediaSources);

        return mediaEntry;
    }

    /**
     * Create {@link PKMediaEntry} with minimum necessary data.
     *
     * @return - the {@link PKMediaEntry} object.
     */
    private PKMediaEntry createSecondMediaEntry() {
        //Create media entry.
        PKMediaEntry mediaEntry = new PKMediaEntry();

        //Set id for the entry.
        mediaEntry.setId(SECOND_ENTRY_ID);

        //Set media entry type. It could be Live,Vod or Unknown.
        //For now we will use Unknown.
        mediaEntry.setMediaType(PKMediaEntry.MediaEntryType.Unknown);

        //Create list that contains at least 1 media source.
        //Each media entry can contain a couple of different media sources.
        //All of them represent the same content, the difference is in it format.
        //For example same entry can contain PKMediaSource with dash and another
        // PKMediaSource can be with hls. The player will decide by itself which source is
        // preferred for playback.
        List<PKMediaSource> mediaSources = createSecondMediaSources();

        //Set media sources to the entry.
        mediaEntry.setSources(mediaSources);

        return mediaEntry;
    }

    /**
     * Create list of {@link PKMediaSource}.
     *
     * @return - the list of sources.
     */
    private List<PKMediaSource> createFirstMediaSources() {
        //Init list which will hold the PKMediaSources.
        List<PKMediaSource> mediaSources = new ArrayList<>();

        //Create new PKMediaSource instance.
        PKMediaSource mediaSource = new PKMediaSource();

        //Set the id.
        mediaSource.setId(FIRST_MEDIA_SOURCE_ID);

        //Set the content url. In our case it will be link to hls source(.m3u8).
        mediaSource.setUrl(FIRST_SOURCE_URL);

        //Set the format of the source. In our case it will be hls.
        mediaSource.setMediaFormat(PKMediaFormat.hls);

        //Add media source to the list.
        mediaSources.add(mediaSource);

        return mediaSources;
    }

    /**
     * Create list of {@link PKMediaSource}.
     *
     * @return - the list of sources.
     */
    private List<PKMediaSource> createSecondMediaSources() {
        //Init list which will hold the PKMediaSources.
        List<PKMediaSource> mediaSources = new ArrayList<>();

        //Create new PKMediaSource instance.
        PKMediaSource mediaSource = new PKMediaSource();

        //Set the id.
        mediaSource.setId(SECOND_MEDIA_SOURCE_ID);

        //Set the content url. In our case it will be link to mp4 source(.mp4).
        mediaSource.setUrl(SECOND_SOURCE_URL);

        //Set the format of the source. In our case it will be mp4.
        mediaSource.setMediaFormat(PKMediaFormat.mp4);

        //Add media source to the list.
        mediaSources.add(mediaSource);

        return mediaSources;
    }

    /**
     * Will add player to the view.
     */
    private void addPlayerToView() {
        //Get the layout, where the player view will be placed.
        LinearLayout layout = (LinearLayout) findViewById(R.id.player_root);
        //Add player view to the layout.
        layout.addView(player.getView());
    }

    /**
     * Just add a simple button which will start/pause playback.
     */
    private void addPlayPauseButton() {
        //Get reference to the play/pause button.
        playPauseButton = (Button) this.findViewById(R.id.play_pause_button);
        //Add clickListener.
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    //If player is playing, change text of the button and pause.
                    playPauseButton.setText(R.string.play_text);
                    player.pause();
                } else {
                    //If player is not playing, change text of the button and play.
                    playPauseButton.setText(R.string.pause_text);
                    player.play();
                }
            }
        });
    }

    /**
     * Just reset the play/pause button text to "Play".
     */
    private void resetPlayPauseButtonToPlayText() {
        playPauseButton.setText(R.string.play_text);
    }
}