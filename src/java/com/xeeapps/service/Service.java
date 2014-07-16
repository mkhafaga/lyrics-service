/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xeeapps.service;

import daos.AlbumDao;
import daos.ArtistDao;
import daos.LyricDao;
import daos.SongDao;
import daos.UltimateDao;
import daos.WildFetcher;
import hulyricsmodel.Album;
import hulyricsmodel.Artist;
import hulyricsmodel.Lyric;
import hulyricsmodel.Song;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import mappers.AlbumsMapper;
import mappers.ArtistsMapper;
import mappers.LyricsMapper;
import mappers.SongsMapper;
import mappers.TopHundredItem;
import mappers.WildResult;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 * REST Web Service
 *
 * @author Khafaga
 */
@Path("service")
public class Service implements ServiceInterface {

    private UltimateDao ultimateDao = new UltimateDao();
    @Context
    private UriInfo context;
    private Conjunction artistsConjunction, albumsConjunction, songsConjunction;

    /**
     * Creates a new instance of Service
     */
    public Service() {
    }

    /**
     * Retrieves representation of an instance of com.xeeapps.service.Service
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("listSongsByArtist")
    @Override
    public SongsMapper listSongsByArtist(@QueryParam("artist") String artist) {



        Artist currentArtist = (Artist) ultimateDao.get(Artist.class, Restrictions.eq("artistName", artist));
        Hibernate.initialize(currentArtist.getSongs());
        Set<Song> songs = currentArtist.getSongs();
        List<Song> songsList = new ArrayList(songs);
        SongsMapper songsWrapper = new SongsMapper();
        songsWrapper.setSongs(songsList);
        return songsWrapper;
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("listArtistsByInitial")
    @Override
    public ArtistsMapper listArtistsByInitial(@QueryParam("initial") String initial) {
        List<Artist> artists = ultimateDao.list(Artist.class, Restrictions.like("artistName", initial, MatchMode.START));
        return new ArtistsMapper(artists);
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("getLyricForSong")
    @Override
    public Lyric getLyricForASong(@QueryParam("songId") Long songId) {
        Song song = (Song) ultimateDao.get(Song.class, Restrictions.eq("id", songId));
        Lyric currentLyric = null;
        Hibernate.initialize(song.getLyrics());
        Set<Lyric> lyrics = song.getLyrics();
        if (lyrics.size() > 0) {
            currentLyric = lyrics.toArray(new Lyric[1])[0];
        }
        return currentLyric;
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("getLyricBySongCriteria")
    @Override
    public Lyric getLyricBySongCriteria(@QueryParam("title") String title, @QueryParam("artistName") String artistName) {
        Artist artist = (Artist) ultimateDao.get(Artist.class, Restrictions.ilike("artistName", artistName));
//          super.startup();
        Conjunction conjunction = Restrictions.conjunction();
        conjunction.add(Restrictions.eq("artist", artist));
        conjunction.add(Restrictions.ilike("title", title));
        Song currentSong = (Song) ultimateDao.get(Song.class, conjunction);
        System.out.println("song: "+currentSong);
        Lyric currentLyric = null;
        Set<Lyric> lyrics = currentSong.getLyrics();
        // super.cleanup();
        if (lyrics.size() > 0) {
            Lyric lyric = lyrics.toArray(new Lyric[1])[0];
            return lyric;
        } else {
            return null;
        }
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("getTopHundred")
    @Override
    public LyricsMapper getTopHundred() {
        List<Lyric> lyricsList = ultimateDao.listWithLimitWithOrder(Lyric.class, 100, Order.desc("views"));
        List<TopHundredItem> topHundredItems = new ArrayList();
        for (Lyric lyric : lyricsList) {
            TopHundredItem topHundredItem = new TopHundredItem();
            Song song = lyric.getSong();
            topHundredItem.setSongTitle(song.getTitle());
            String artistName = ((Song) ultimateDao.get(Song.class, Restrictions.eq("id", song.getId()))).getArtist().getArtistName();
            topHundredItem.setArtistName(artistName);
            topHundredItems.add(topHundredItem);
        }
        LyricsMapper lyrics = new LyricsMapper(topHundredItems);

        return lyrics;
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("search")
    @Override
    public WildResult search(@QueryParam("q") String query) {
        artistsConjunction = Restrictions.conjunction();
        albumsConjunction = Restrictions.conjunction();
        songsConjunction = Restrictions.conjunction();
        String[] slices = query.split(" ");
        for (String slice : slices) {
            artistsConjunction.add(Restrictions.ilike("artistName", slice, MatchMode.ANYWHERE));
            albumsConjunction.add(Restrictions.ilike("albumName", slice, MatchMode.ANYWHERE));
            songsConjunction.add(Restrictions.ilike("title", slice, MatchMode.ANYWHERE));
        }
        WildResult wildResult = new WildResult();
        wildResult.setArtistsMapper(new ArtistsMapper(ultimateDao.list(Artist.class, artistsConjunction)));
        wildResult.setAlbumsMapper(new AlbumsMapper(ultimateDao.list(Album.class, albumsConjunction)));
        wildResult.setSongsMapper(new SongsMapper(ultimateDao.list(Song.class, songsConjunction)));
        return wildResult;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("updateViews")
    @Override
    public Boolean updateViews(@QueryParam("id") Long id, @QueryParam("views") Long views) {
        Lyric lyric = (Lyric) ultimateDao.get(Lyric.class, Restrictions.eq("id", id));
        Hibernate.initialize(lyric.getSong());
        lyric.setViews(views);
        List<Lyric> lyrics = new ArrayList<Lyric>();
        lyrics.add(lyric);
        boolean status = ultimateDao.update(lyrics);
        return status;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getLyricsCount")
    @Override
    public Number getLyricsCount() {
        return ultimateDao.getCount(Lyric.class);
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("getLyricOfTheDay")
    @Override
    public Lyric getLyricOfTheDay() {
        Lyric lyric = (Lyric) ultimateDao.listWithLimitWithOrder(Lyric.class, 1, Order.desc("views")).get(0);
        return lyric;
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("listSongsByAlbum")
    @Override
    public SongsMapper listSongsByAlbum(@QueryParam("albumId") Long albumId) {
        Album currentAlbum = (Album) ultimateDao.get(Album.class, Restrictions.eq("id", albumId));
        Hibernate.initialize(currentAlbum.getSongs());
        Set<Song> songs = currentAlbum.getSongs();
        return new SongsMapper(new ArrayList(songs));
    }
}
