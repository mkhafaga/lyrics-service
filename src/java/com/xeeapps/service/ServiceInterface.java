/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xeeapps.service;

import hulyricsmodel.Lyric;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import mappers.ArtistsMapper;
import mappers.LyricsMapper;
import mappers.SongsMapper;
import mappers.WildResult;

/**
 *
 * @author Khafaga
 */
public interface ServiceInterface {

    @GET
    @Produces(value = MediaType.TEXT_XML)
    @Path(value = "getLyricBySongCriteria")
    Lyric getLyricBySongCriteria(@QueryParam(value = "title") String title, @QueryParam(value = "artistName") String artistName);

    @GET
    @Produces(value = MediaType.TEXT_XML)
    @Path(value = "getLyricForSong")
    Lyric getLyricForASong(@QueryParam(value = "songId") Long songId);

    @GET
    @Produces(value = MediaType.TEXT_XML)
    @Path(value = "getLyricOfTheDay")
    Lyric getLyricOfTheDay();

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @Path(value = "getLyricsCount")
    Number getLyricsCount();

    @GET
    @Produces(value = MediaType.TEXT_XML)
    @Path(value = "getTopHundred")
    LyricsMapper getTopHundred();

    @GET
    @Produces(value = MediaType.TEXT_XML)
    @Path(value = "listArtistsByInitial")
    ArtistsMapper listArtistsByInitial(@QueryParam(value = "initial") String initial);

    @GET
    @Produces(value = MediaType.TEXT_XML)
    @Path(value = "listSongsByAlbum")
    SongsMapper listSongsByAlbum(@QueryParam(value = "albumId") Long albumId);

    /**
     * Retrieves representation of an instance of com.xeeapps.service.Service
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(value = MediaType.TEXT_XML)
    @Path(value = "listSongsByArtist")
    SongsMapper listSongsByArtist(@QueryParam(value = "artist") String artist);

    @GET
    @Produces(value = MediaType.TEXT_XML)
    @Path(value = "search")
    WildResult search(@QueryParam(value = "q") String query);

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @Path(value = "updateViews")
    Boolean updateViews(@QueryParam(value = "id") Long id, @QueryParam(value = "views") Long views);
     
  
}
