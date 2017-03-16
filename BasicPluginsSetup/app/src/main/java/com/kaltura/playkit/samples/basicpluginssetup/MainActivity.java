package com.kaltura.playkit.samples.basicpluginssetup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kaltura.playkit.PKEvent;
import com.kaltura.playkit.PKLog;
import com.kaltura.playkit.PKMediaConfig;
import com.kaltura.playkit.PKMediaEntry;
import com.kaltura.playkit.PKMediaFormat;
import com.kaltura.playkit.PKMediaSource;
import com.kaltura.playkit.PKPluginConfigs;
import com.kaltura.playkit.PlayKitManager;
import com.kaltura.playkit.Player;
import com.kaltura.playkit.PlayerEvent;
import com.kaltura.playkit.player.PKTracks;
import com.kaltura.playkit.plugins.KalturaStatsPlugin;
import com.kaltura.playkit.plugins.TVPAPIAnalyticsPlugin;
import com.kaltura.playkit.plugins.Youbora.YouboraPlugin;
import com.kaltura.playkit.plugins.ads.AdError;
import com.kaltura.playkit.plugins.ads.AdEvent;
import com.kaltura.playkit.plugins.ads.ima.IMAConfig;
import com.kaltura.playkit.plugins.ads.ima.IMAPlugin;
import com.kaltura.playkit.samples.basicpluginssetup.plugins.ConverterYoubora;

import java.util.ArrayList;
import java.util.List;

import static com.kaltura.playkit.PlayerEvent.Type.CAN_PLAY;
import static com.kaltura.playkit.PlayerEvent.Type.ENDED;
import static com.kaltura.playkit.PlayerEvent.Type.ERROR;
import static com.kaltura.playkit.PlayerEvent.Type.PAUSE;
import static com.kaltura.playkit.PlayerEvent.Type.PLAY;
import static com.kaltura.playkit.PlayerEvent.Type.PLAYING;
import static com.kaltura.playkit.PlayerEvent.Type.SEEKED;
import static com.kaltura.playkit.PlayerEvent.Type.SEEKING;
import static com.kaltura.playkit.PlayerEvent.Type.TRACKS_AVAILABLE;


public class MainActivity extends AppCompatActivity {
    private static final PKLog log = PKLog.get("EventReg");

    private static final int START_POSITION = 0;

    //The url of the source to play
    private static final String SOURCE_URL = "https://cdnapisec.kaltura.com/p/2215841/sp/221584100/playManifest/entryId/1_w9zx2eti/protocol/https/format/applehttp/falvorIds/1_1obpcggb,1_yyuvftfz,1_1xdbzoa6,1_k16ccgto,1_djdf6bk8/a.m3u8";
    private static final String STATS_KALTURA_COM = "https://stats.kaltura.com/api_v3/index.php";
    public static final String ANALYTIC_TRIGGER_INTERVAL = "30"; // in seconds

    private static final String ENTRY_ID = "entry_id";
    private static final String MEDIA_SOURCE_ID = "source_id";

    private Player player;
    private AdEvent.AdStartedEvent adStartedEventInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PlayKitManager.registerPlugins(this, IMAPlugin.factory);
        PlayKitManager.registerPlugins(this, YouboraPlugin.factory);
        PlayKitManager.registerPlugins(this, KalturaStatsPlugin.factory);
        PlayKitManager.registerPlugins(this, TVPAPIAnalyticsPlugin.factory);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenu();
            }
        });
    }

    @Override
    protected void onPause() {
        if (player != null) {
            player.pause();
            player.onApplicationPaused();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.onApplicationResumed();
            player.play();
        }
    }

    void showMenu() {
        final Context context = this;
        new AlertDialog.Builder(context)
                .setItems(new String[]{"Play", "Pause"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(context, "Selected " + which, Toast.LENGTH_SHORT).show();
                        if (which == 0) {
                            playDemoVideo();
                        } else if (which == 1) {
                            pauseDemoVideo();
                        }
                    }
                })
                .show();
    }

    private void pauseDemoVideo() {
        if (player != null) {
            player.pause();
        }
    }

    private void playDemoVideo() {
        if (player != null) {
            player.play();
            return;
        }



        //First. Create PKMediaConfig object.
        PKMediaConfig mediaConfig = new PKMediaConfig()
                // You can configure the start position for it.
                // by default it will be 0.
                // If start position is grater then duration of the source it will be reset to 0.
                .setStartPosition(START_POSITION);

        //Second. Create PKMediaEntry object.
        PKMediaEntry mediaEntry = createMediaEntry();


        //Add it to the mediaConfig.
        mediaConfig.setMediaEntry(mediaEntry);


        PKPluginConfigs pluginConfig = new PKPluginConfigs();
        configureIMAPlugin(pluginConfig);
        configureYouboraPlugin(pluginConfig);
        configureKalturaStatsPlugin(pluginConfig);
        configureTVPapiPlugin(pluginConfig);

        //Create instance of the player.
        player = PlayKitManager.loadPlayer(pluginConfig, this);

        //Get the layout, where the player view will be placed.
        LinearLayout layout = (LinearLayout) findViewById(R.id.player_root);
        //Add player view to the layout.
        layout.addView(player.getView());

        //Prepare player with media configuration.
        player.prepare(mediaConfig);

        //Start playback.
        player.play();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Create {@link PKMediaEntry} with minimum necessary data.
     *
     * @return - the {@link PKMediaEntry} object.
     */
    private PKMediaEntry createMediaEntry() {
        //Create media entry.
        PKMediaEntry mediaEntry = new PKMediaEntry();

        //Set id for the entry.
        mediaEntry.setId(ENTRY_ID);

        //Set media entry type. It could be Live,Vod or Unknown.
        //For now we will use Unknown.
        mediaEntry.setMediaType(PKMediaEntry.MediaEntryType.Unknown);

        //Create list that contains at least 1 media source.
        //Each media entry can contain a couple of different media sources.
        //All of them represent the same content, the difference is in it format.
        //For example same entry can contain PKMediaSource with dash and another
        // PKMediaSource can be with hls. The player will decide by itself which source is
        // preferred for playback.
        List<PKMediaSource> mediaSources = createMediaSources();

        //Set media sources to the entry.
        mediaEntry.setSources(mediaSources);

        return mediaEntry;
    }

    /**
     * Create list of {@link PKMediaSource}.
     *
     * @return - the list of sources.
     */
    private List<PKMediaSource> createMediaSources() {
        //Init list which will hold the PKMediaSources.
        List<PKMediaSource> mediaSources = new ArrayList<>();

        //Create new PKMediaSource instance.
        PKMediaSource mediaSource = new PKMediaSource();

        //Set the id.
        mediaSource.setId(MEDIA_SOURCE_ID);

        //Set the content url. In our case it will be link to hls source(.m3u8).
        mediaSource.setUrl(SOURCE_URL);

        //Set the format of the source. In our case it will be hls.
        mediaSource.setMediaFormat(PKMediaFormat.hls);

        //Add media source to the list.
        mediaSources.add(mediaSource);

        return mediaSources;
    }

    protected void setPlayerListeners() {

        player.addEventListener(new PKEvent.Listener() {

                                    @Override
                                    public void onEvent(PKEvent event) {
                                        log.d("addEventListener " + event.eventType());
                                        log.d("Player Total duration => " + player.getDuration());
                                        log.d("Player Current duration => " + player.getCurrentPosition());


                                        Enum receivedEventType = event.eventType();
                                        if (event instanceof PlayerEvent) {
                                            switch (((PlayerEvent) event).type) {
                                                case CAN_PLAY:
                                                    log.d("Received " + CAN_PLAY.name());
                                                    break;
                                                case PLAY:
                                                    log.d("Received " + PLAY.name());
                                                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                                    break;
                                                case PLAYING:
                                                    log.d("Received " + PLAYING.name());
                                                    break;
                                                case PAUSE:
                                                    log.v("Received " + PAUSE.name());
                                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                                    break;
                                                case SEEKING:
                                                    log.d("Received " + SEEKING.name());
                                                    break;
                                                case SEEKED:
                                                    log.d("Received " + SEEKED.name());
                                                    break;
                                                case ENDED:
                                                    log.d("Received " + ENDED.name());
                                                    break;
                                                case TRACKS_AVAILABLE:
                                                    PKTracks tracks = ((PlayerEvent.TracksAvailable) event).getPKTracks();
                                                    log.d("Received " + TRACKS_AVAILABLE.name());
                                                    break;
                                                case ERROR:
                                                    log.d("Received " + ERROR.name());
                                                    PlayerEvent.ExceptionInfo exceptionInfo = (PlayerEvent.ExceptionInfo) event;
                                                    String errorMsg = "Player error occurred.";
                                                    if (exceptionInfo != null && exceptionInfo.getException() != null && exceptionInfo.getException().getMessage() != null) {
                                                        errorMsg = exceptionInfo.getException().getMessage();
                                                    }
                                                    log.e("Player Error: " + errorMsg);

                                                    break;
                                            }
                                        } else if (event instanceof AdEvent) {
                                            switch (((AdEvent) event).type) {
                                                case LOADED:
                                                    log.d("Received " + AdEvent.Type.LOADED.name());
                                                    break;
                                                case CUEPOINTS_CHANGED:
                                                    log.d("Received " + AdEvent.Type.CUEPOINTS_CHANGED.name());
                                                    break;
                                                case ALL_ADS_COMPLETED:
                                                    log.v("Received " + AdEvent.Type.ALL_ADS_COMPLETED.name());
                                                    break;
                                                case AD_BREAK_IGNORED:
                                                    log.d("Received " + AdEvent.Type.AD_BREAK_IGNORED.name());
                                                    player.play();
                                                    break;
                                                case CONTENT_PAUSE_REQUESTED:
                                                    log.d("Received " + AdEvent.Type.CONTENT_PAUSE_REQUESTED.name());
                                                    break;
                                                // case AD_DISPLAYED_AFTER_CONTENT_PAUSE:
                                                //     log.v("Received " + AdEvent.Type.AD_DISPLAYED_AFTER_CONTENT_PAUSE.name());
                                                //     break;
                                                case CONTENT_RESUME_REQUESTED:
                                                    log.v("Received " + AdEvent.Type.CONTENT_RESUME_REQUESTED.name());
                                                    break;
                                                case STARTED:
                                                    log.v("Received " + AdEvent.Type.STARTED.name());
                                                    adStartedEventInfo = (AdEvent.AdStartedEvent) event;
                                                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                                    break;
                                                case PAUSED:
                                                    log.d("Received " + AdEvent.Type.PAUSED.name());
                                                    break;
                                                case TAPPED:
                                                    break;
                                                case COMPLETED:
                                                    log.d("Received " + AdEvent.Type.COMPLETED.name());
                                                    break;
                                                case SKIPPED:
                                                    log.d("Received " + AdEvent.Type.SKIPPED.name());
                                                    break;
                                                case CLICKED:
                                                    log.d("Received " + AdEvent.Type.CLICKED.name());
                                                    break;
                                            }
                                        } else if (event instanceof AdError) {
                                            switch (((AdError) event).errorType) {
                                                case ADS_REQUEST_NETWORK_ERROR:
                                                case INTERNAL_ERROR:
                                                case VAST_MALFORMED_RESPONSE:
                                                case UNKNOWN_AD_RESPONSE:
                                                case VAST_LOAD_TIMEOUT:
                                                case VAST_TOO_MANY_REDIRECTS:
                                                case VIDEO_PLAY_ERROR:
                                                case VAST_MEDIA_LOAD_TIMEOUT:
                                                case VAST_LINEAR_ASSET_MISMATCH:
                                                case OVERLAY_AD_PLAYING_FAILED:
                                                case OVERLAY_AD_LOADING_FAILED:
                                                case VAST_NONLINEAR_ASSET_MISMATCH:
                                                case COMPANION_AD_LOADING_FAILED:
                                                case UNKNOWN_ERROR:
                                                case VAST_EMPTY_RESPONSE:
                                                case FAILED_TO_REQUEST_ADS:
                                                case VAST_ASSET_NOT_FOUND:
                                                case INVALID_ARGUMENTS:
                                                case QUIET_LOG_ERROR:
                                                case PLAYLIST_NO_CONTENT_TRACKING:
                                                    log.e("Player Error: Play Called");
                                                    player.play();
                                                    break;
                                            }
                                        }
                                    }

                                },
                PlayerEvent.Type.PLAY, PLAYING,
                PlayerEvent.Type.PAUSE, CAN_PLAY,
                SEEKING, SEEKED,
                ENDED, TRACKS_AVAILABLE,
                PlayerEvent.Type.ERROR,

                AdEvent.Type.LOADED, AdEvent.Type.SKIPPED,
                AdEvent.Type.TAPPED, AdEvent.Type.CONTENT_PAUSE_REQUESTED,
                AdEvent.Type.CONTENT_RESUME_REQUESTED, AdEvent.Type.STARTED,
                AdEvent.Type.PAUSED, AdEvent.Type.RESUMED,
                AdEvent.Type.COMPLETED, AdEvent.Type.ALL_ADS_COMPLETED,
                AdEvent.Type.CUEPOINTS_CHANGED, AdEvent.Type.CLICKED,
                AdEvent.Type.AD_BREAK_IGNORED, //AdEvent.Type.AD_DISPLAYED_AFTER_CONTENT_PAUSE,

                AdError.Type.VAST_EMPTY_RESPONSE, AdError.Type.COMPANION_AD_LOADING_FAILED,
                AdError.Type.FAILED_TO_REQUEST_ADS, AdError.Type.INTERNAL_ERROR, AdError.Type.OVERLAY_AD_LOADING_FAILED,
                AdError.Type.PLAYLIST_NO_CONTENT_TRACKING, AdError.Type.UNKNOWN_ERROR,
                AdError.Type.VAST_LINEAR_ASSET_MISMATCH, AdError.Type.VAST_MALFORMED_RESPONSE,
                AdError.Type.QUIET_LOG_ERROR, AdError.Type.VAST_LOAD_TIMEOUT,
                AdError.Type.ADS_REQUEST_NETWORK_ERROR, AdError.Type.INVALID_ARGUMENTS,
                AdError.Type.VAST_TOO_MANY_REDIRECTS);

        player.addStateChangeListener(new PKEvent.Listener() {
            @Override
            public void onEvent(PKEvent event) {

                PlayerEvent.StateChanged stateChanged = (PlayerEvent.StateChanged) event;
                log.v("addStateChangeListener " + event.eventType() + " = " + stateChanged.newState);
                switch (stateChanged.newState) {
                    case IDLE:
                        log.d("StateChange Idle");
                        break;
                    case LOADING:
                        log.d("StateChange Loading");
                        break;
                    case READY:
                        log.d("StateChange Ready");
                        break;
                    case BUFFERING:
                        log.d("StateChange Buffering");
                        break;
                }
            }
        });
    }

    private void configureIMAPlugin(PKPluginConfigs pluginConfig) {
        String adTagUrl = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=";
        IMAConfig adsConfig = new IMAConfig().setAdTagURL(adTagUrl).setVideoMimeTypes(null);
        pluginConfig.setPluginConfig(IMAPlugin.factory.getName(), adsConfig.toJSONObject());
    }

    private void configureYouboraPlugin(PKPluginConfigs pluginConfig) {
        JsonObject youboraConfigEntry = new JsonObject();
        youboraConfigEntry.addProperty("accountCode", "YOUR_ACCOUNT_CODE");
        youboraConfigEntry.addProperty("username", "YOUR_YOUBORA_USER_NAME");
        youboraConfigEntry.addProperty("haltOnError", true);
        youboraConfigEntry.addProperty("enableAnalytics", true);

        JsonObject mediaEntry = new JsonObject();
        mediaEntry.addProperty("title", "YOUR_MEDIA_TITLE");

        JsonObject adsEntry = new JsonObject();
        adsEntry.addProperty("adsExpected", true);
        adsEntry.addProperty("title", adStartedEventInfo != null ? adStartedEventInfo.adInfo.getAdTitle() : "");
        adsEntry.addProperty("campaign", "");

        JsonObject extraParamEntry = new JsonObject();
        //extraParamEntry.addProperty("param1", "Mobile");
        extraParamEntry.addProperty("param2", "playKitPlayer");
        extraParamEntry.addProperty("param3", "");

        JsonObject propertiesEntry = new JsonObject();
        propertiesEntry.addProperty("genre", "");
        propertiesEntry.addProperty("type", "");
        propertiesEntry.addProperty("transaction_type", "");
        propertiesEntry.addProperty("year", "");
        propertiesEntry.addProperty("cast", "");
        propertiesEntry.addProperty("director", "");
        propertiesEntry.addProperty("owner", "");
        propertiesEntry.addProperty("parental", "");
        propertiesEntry.addProperty("price", "");
        propertiesEntry.addProperty("rating", "");
        propertiesEntry.addProperty("audioType", "");
        propertiesEntry.addProperty("audioChannels", "");
        propertiesEntry.addProperty("device", "");
        propertiesEntry.addProperty("quality", "");

        ConverterYoubora converterYoubora = new ConverterYoubora(youboraConfigEntry,
                adsEntry, extraParamEntry, propertiesEntry);

        pluginConfig.setPluginConfig(YouboraPlugin.factory.getName(), converterYoubora.toJson());
    }

    private void configureTVPapiPlugin(PKPluginConfigs pluginConfig) {
        JsonObject paramsEntry = new JsonObject();
        paramsEntry.addProperty("fileId", "1234"); //YOUR_FileID
        paramsEntry.addProperty("baseUrl", "http://tvpapi-as.ott.kaltura.com/v3_9/gateways/jsonpostgw.aspx?");
        paramsEntry.addProperty("timerInterval", 30);

        String initObj = "            {\n" +
                "              \"SiteGuid\": \"716158\",\n" +
                "              \"ApiUser\": \"tvpapi_198\",\n" +
                "              \"DomainID\": \"354531\",\n" +
                "              \"UDID\": \"e8aa934c-eae4-314f-b6a0-f55e96498786\",\n" +
                "              \"ApiPass\": \"11111\",\n" +
                "              \"Locale\": {\n" +
                "                \"LocaleUserState\": \"Unknown\",\n" +
                "                \"LocaleCountry\": \"\",\n" +
                "                \"LocaleDevice\": \"\",\n" +
                "                \"LocaleLanguage\": \"en\"\n" +
                "              },\n" +
                "              \"Platform\": \"Cellular\"\n" +
                "            }";
        JsonElement value = new Gson().fromJson(initObj, JsonElement.class);
        paramsEntry.add("initObj", value);

        pluginConfig.setPluginConfig(TVPAPIAnalyticsPlugin.factory.getName(), paramsEntry);
    }

    private void configureKalturaStatsPlugin(PKPluginConfigs pluginConfig) {
        JsonObject pluginEntry = new JsonObject();
        pluginEntry.addProperty("sessionId", "b3460681-b994-6fad-cd8b-f0b65736e837");
        pluginEntry.addProperty("uiconfId", Integer.parseInt("1234")); //YOUR_PLAYER_UI_CONF_ID
        pluginEntry.addProperty("baseUrl", STATS_KALTURA_COM);
        pluginEntry.addProperty("partnerId", Integer.parseInt("1234")); //YOUR_PARTNER_ID
        pluginEntry.addProperty("timerInterval", Integer.parseInt(ANALYTIC_TRIGGER_INTERVAL));
        pluginConfig.setPluginConfig(KalturaStatsPlugin.factory.getName(), pluginEntry);
    }

}