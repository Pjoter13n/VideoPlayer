package pl.piotrek.main;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.Equalizer;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurface;

public class SecondScreenPlayer {



	    private final MediaPlayerFactory mediaPlayerFactory;
	    private final EmbeddedMediaPlayer mediaPlayer;
	    private JFrame frame;
	    private Canvas canvas;
	    private Equalizer equalizer;
	    private List<String> currentPlaylist;
	    private int currentPlaylistIndex = 0;
	    private String mainFilmTitle;
	    private List<String> playlist;
	    private int currentIndex = 0;
	    
	    public MediaPlayer getMediaPlayer() {
	        return mediaPlayer;
	    }

	    public SecondScreenPlayer() {
	        mediaPlayerFactory = new MediaPlayerFactory("--video-on-top", "--no-video-title-show");
	    	initializeEqualizer();
	        mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();

	    }
	    public void playPlaylist(List<Path> videos) {
	        playlist = new ArrayList<>();
	        for (Path p : videos) {
	            playlist.add(p.toString());
	        }
	        currentIndex = 0;
	        playCurrent();
	        
	        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
	            @Override
	            public void finished(MediaPlayer mediaPlayer) {
	                playNext();
	            }
	            @Override
	            public void error(MediaPlayer mediaPlayer) {
	                System.err.println("Błąd odtwarzania: " + getCurrentMedia());
	                playNext();
	            }
	        });
	    }

	    private void playCurrent() {
	        if (playlist != null && currentIndex < playlist.size()) {
	            String media = playlist.get(currentIndex);
	            System.out.println("Odtwarzam: " + media);
	            mediaPlayer.media().play(media);
	        } else {
	            System.out.println("Koniec playlisty");
	        }
	    }

	    private void playNext() {
	        currentIndex++;
	        if (playlist != null && currentIndex < playlist.size()) {
	            playCurrent();
	        } else {
	            System.out.println("Playlista zakończona");
	        }
	    }

	    private String getCurrentMedia() {
	        if (playlist != null && currentIndex < playlist.size()) {
	            return playlist.get(currentIndex);
	        }
	        return null;
	    }
	    public void setOnEndOfMedia(Runnable action) {
	        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
	            @Override
	            public void finished(MediaPlayer mediaPlayer) {
	                action.run();
	            }
	        });
	    }


	    public void playOnSecondScreen(String mediaPath) {
	        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	        GraphicsDevice[] screens = ge.getScreenDevices();

	        GraphicsDevice screenToUse;
	        if (screens.length < 2) {
	            System.out.println("Brak drugiego ekranu, używam głównego");
	            screenToUse = screens[0];
	        } else {
	            System.out.println("Używam drugiego ekranu");
	            screenToUse = screens[1];
	        }

	        Rectangle bounds = screenToUse.getDefaultConfiguration().getBounds();

	        frame = new JFrame("Wideo na drugim ekranie") {
	            @Override
	            public void processWindowEvent(WindowEvent e) {
	                if (e.getID() == WindowEvent.WINDOW_CLOSING) {
	                    return;
	                }
	                super.processWindowEvent(e);
	            }
	        };


	        frame.setUndecorated(true);
	        frame.setResizable(false);
	        frame.setAlwaysOnTop(true);
	        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	        frame.setBounds(bounds);
	        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
	            @Override
	            public void componentMoved(java.awt.event.ComponentEvent e) {
	                frame.setLocation(bounds.x, bounds.y);
	            }
	        });

	        frame.setLayout(new BorderLayout());

	        canvas = new Canvas();
	        canvas.setSize(bounds.width, bounds.height);

	        JPanel panel = new JPanel(new BorderLayout());
	        panel.add(canvas, BorderLayout.CENTER);

	        frame.add(panel, BorderLayout.CENTER);
	        frame.setFocusableWindowState(false);
	        frame.setVisible(false);
	        SwingUtilities.invokeLater(() -> {
	            frame.toFront();
	            frame.setAlwaysOnTop(true); 
	            frame.setExtendedState(frame.NORMAL);

	        });
	        

	        new Thread(() -> {
	            try {
	                while (frame != null && frame.isDisplayable()) {
	                	SwingUtilities.invokeLater(() -> {
	                	    if (!frame.isVisible()) frame.setVisible(true);
	                	    if (!frame.isAlwaysOnTop()) frame.setAlwaysOnTop(true);
	                	    if (frame.getExtendedState() == frame.ICONIFIED) {
	                	        frame.setExtendedState(frame.NORMAL);
	                	        frame.toFront();
	                	    }
	                	});
	                    Thread.sleep(2); 
	                }
	            } catch (InterruptedException e) {
	            }
	        }).start();

	        frame.addWindowStateListener(e -> {
	            int newState = e.getNewState();

	
	            if ((newState & frame.ICONIFIED) == frame.ICONIFIED) {
	                SwingUtilities.invokeLater(() -> {
	                    frame.setExtendedState(frame.NORMAL);
	                    frame.setAlwaysOnTop(true); 
	                    frame.toFront();
	                    frame.requestFocus();
	                });
	            }

	  
	            if ((newState & frame.MAXIMIZED_BOTH) == frame.MAXIMIZED_BOTH) {
	                SwingUtilities.invokeLater(() -> {
	                    frame.setExtendedState(frame.NORMAL);
	                });
	            }
	        });

	        VideoSurface videoSurface = mediaPlayerFactory.videoSurfaces().newVideoSurface(canvas);
	        mediaPlayer.videoSurface().set(videoSurface);

	        screenToUse.setFullScreenWindow(frame);
	        SwingUtilities.invokeLater(() -> {
	            try {
	                Thread.sleep(300); 
	                frame.repaint();
	                canvas.repaint();
	                boolean started = mediaPlayer.media().play(mediaPath);
	                if (!started) {
	                    System.out.println("Nie udało się rozpocząć odtwarzania: " + mediaPath);
	                }
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        });
	        new javax.swing.Timer(1000, evt -> {
	            frame.toFront();
	            frame.setAlwaysOnTop(true);
	            frame.setExtendedState(frame.NORMAL);
	            frame.requestFocus();
	        }).start();
	    }
	    


	    
	    

	    @SuppressWarnings("deprecation")
		public void stop() {
	        mediaPlayer.controls().stop();
	        if(frame !=null) {
	        	frame.dispose();
	        	canvas.setVisible(false);
	        	canvas.disable();
	        	
	        }
	    }

	    public void pause() {
	        mediaPlayer.controls().pause();
	    }
	    
	    public long getCurrentTime() {
	        return mediaPlayer.status().time();
	    }

	    public long getMediaDuration() {
	        return mediaPlayer.media().info().duration();
	    }
	    public void seek(long millis) {
	        mediaPlayer.controls().setTime(millis);
	    }
	    public void setVolume(int volume) {
	        if (mediaPlayer != null) {
	            mediaPlayer.audio().setVolume(volume);
	        }
	    }
	    public void setBrightness(double value) {
	        if (mediaPlayer != null) {
	            mediaPlayer.video().setAdjustVideo(true);
	            mediaPlayer.video().setBrightness((float) ((value + 100) / 200)); 
	        }
	    }

	    public void setContrast(double value) {
	        if (mediaPlayer != null) {
	            mediaPlayer.video().setAdjustVideo(true);
	            mediaPlayer.video().setContrast((float) ((value + 100) / 200));
	        }
	    }

	    public void setSaturation(double value) {
	        if (mediaPlayer != null) {
	            mediaPlayer.video().setAdjustVideo(true);
	            mediaPlayer.video().setSaturation((float) ((value + 100) / 200));
	        }
	    }
	    public void resetAdjustments() {
	        if (mediaPlayer != null) {
	            mediaPlayer.video().setAdjustVideo(true);
	            mediaPlayer.video().setBrightness(0.5f);
	            mediaPlayer.video().setContrast(0.5f);
	            mediaPlayer.video().setSaturation(0.5f);
	        }
	    }
	    public void initializeEqualizer() {
	        if (equalizer == null) {
	            equalizer = new Equalizer(0);
	        }

	        if (mediaPlayer != null) {
	            mediaPlayer.audio().setEqualizer(equalizer);
	        }
	    }

	    public void setEqualizerBand(int bandIndex, float gainDb) {
	        if (equalizer == null) {
	            initializeEqualizer();
	        }

	        if (bandIndex >= 0 && bandIndex < 10) { 
	            equalizer.setAmp(bandIndex, gainDb);
	            if (mediaPlayer != null) {
	                mediaPlayer.audio().setEqualizer(equalizer);
	            }
	        } else {
	            System.err.println("❌ BŁĄD: Nieprawidłowy indeks pasma EQ: " + bandIndex);
	        }
	    }

	    
}