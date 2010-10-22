/*
 * CommunityRecentSubmissions.java
 *
 * Version: $Revision: 5497 $
 *
 * Date: $Date: 2010-10-20 23:06:10 +0200 (wo, 20 okt 2010) $
 *
 * Copyright (c) 2002, Hewlett-Packard Company and Massachusetts
 * Institute of Technology.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Hewlett-Packard Company nor the name of the
 * Massachusetts Institute of Technology nor the names of their
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */
package org.dspace.app.xmlui.aspect.discovery;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.ReferenceSet;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Constants;
import org.dspace.discovery.SearchUtils;
import org.xml.sax.SAXException;

/**
 * Renders a list of recently submitted items for the community by using discovery
 *
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 */
public class CommunityRecentSubmissions extends AbstractFiltersTransformer
{

    private static final Logger log = Logger.getLogger(CommunityRecentSubmissions.class);

    private static final Message T_head_recent_submissions =
            message("xmlui.ArtifactBrowser.CollectionViewer.head_recent_submissions");



    /**
     * Display a single community (and refrence any sub communites or
     * collections)
     */
    public void addBody(Body body) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException
    {

        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        if (!(dso instanceof Community))
        {
            return;
        }

        // Build the community viewer division.
        Division home = body.addDivision("community-home", "primary repository community");

        performSearch(dso);

        Division lastSubmittedDiv = home
                .addDivision("community-recent-submission", "secondary recent-submission");

        lastSubmittedDiv.setHead(T_head_recent_submissions);

        ReferenceSet lastSubmitted = lastSubmittedDiv.addReferenceSet(
                "community-last-submitted", ReferenceSet.TYPE_SUMMARY_LIST,
                null, "recent-submissions");

        for (SolrDocument doc : queryResults.getResults()) {
            lastSubmitted.addReference(SearchUtils.findDSpaceObject(context, doc));
        }
    }


    /**
     * Get the recently submitted items for the given community or collection.
     *
     * @param scope The comm/collection.
     * @return the response of the query
     */
    public void performSearch(DSpaceObject scope) {


        if(queryResults != null)
        {
            return;
        }// queryResults;

        queryArgs = prepareDefaultFilters("community");

        queryArgs.setQuery("search.resourcetype:" + Constants.ITEM);

        queryArgs.setRows(SearchUtils.getConfig().getInt("solr.recent-submissions.size", 5));

        String sortField = SearchUtils.getConfig().getString("recent.submissions.sort-option");
        if(sortField != null){
            queryArgs.setSortField(
                    sortField,
                    SolrQuery.ORDER.desc
            );
        }
        /* Set the communities facet filters */
        queryArgs.setFilterQueries("location:m" + scope.getID());

        try {
            queryResults =  getSearchService().search(queryArgs);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
        }


    }

}