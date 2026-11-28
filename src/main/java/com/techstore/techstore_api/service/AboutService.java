package com.techstore.techstore_api.service;

import com.techstore.techstore_api.model.AboutContent;

import java.util.Map;

public interface AboutService {

    /** Lecture publique : contenu publié + statistiques dynamiques réelles injectées */
    Map<String, Object> getPublicAboutData();

    /** Lecture admin : retourne la version LIVE ou DRAFT */
    AboutContent getAdminAboutContent();

    /** Sauvegarde admin (mode draft ou live) */
    AboutContent saveAdminAboutContent(AboutContent content, boolean publishDirectly);

    /** Publication explicite du draft vers live */
    AboutContent publishDraft();
}
