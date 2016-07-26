/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.abondar.experimental.eventsearch;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author alex
 */
public class SearchData {

    private final String indexPath = "/home/abondar/EventSearch/jsons/";
    private final Boolean create = true;

    public void IndexFiles() {
        try {
            Directory dir = FSDirectory.open(new File(indexPath));
                   // FSDirectory.open(new File(indexPath));

            Analyzer an = new StandardAnalyzer(Version.LUCENE_44);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_44, an);

            if (create) {
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            } else {
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            }

            IndexWriter writer = new IndexWriter(dir, iwc);
            Path docs = Paths.get(indexPath);
            indexDocs(writer, docs);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(SearchData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void indexDocs(final IndexWriter iw, Path path) throws IOException {

        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        indexDoc(iw, file, attrs.lastModifiedTime().toMillis());
                    } catch (IOException ignore) {
                        // don't index files that can't be read.
                    }
                    return FileVisitResult.CONTINUE;
                }

            });
        } else {

            indexDoc(iw, path, Files.getLastModifiedTime(path).toMillis());
        }
    }

    public void indexDoc(IndexWriter iw, Path file, long lastModified) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {

            Document doc = new Document();
            Field pathField = new StringField("path", file.toString(), Field.Store.YES);
            doc.add(pathField);
            ObjectMapper mapper = new ObjectMapper();
            Event eb = mapper.readValue(new File(file.toString()), Event.class);
            doc.add(new TextField("category", eb.getCategory(), Field.Store.YES));

            if (iw.getConfig().getOpenMode() == OpenMode.CREATE) {
                iw.addDocument(doc);
                for (IndexableField ifd : doc.getFields()) {
                    System.out.println(ifd.stringValue() + "  " + ifd.name());
                }
                System.out.println("adding " + file);

            } else {

                iw.updateDocument(new Term("path", file.toString()), doc);
                System.out.println("updating " + file);
            }

        }
    }

    public String[] getEventData(String evCategory) throws IOException, org.apache.lucene.queryparser.classic.ParseException {

        Analyzer an = new StandardAnalyzer(Version.LUCENE_44);

        Query q = new QueryParser(Version.LUCENE_44, "category", an).parse(evCategory);
        Directory dir = FSDirectory.open(new File(indexPath));

        IndexReader reader = DirectoryReader.open(dir);
        Integer numDocs = reader.numDocs();
      
        
        IndexSearcher searcher = new IndexSearcher(reader);
        ScoreDoc[] hits = searcher.search(q, numDocs).scoreDocs;
        String[] res = new String[hits.length];
     
        for (int i = 0; i < hits.length; i++) {
            int docId = hits[i].doc;

            res[i]=searcher.doc(docId).get("path");
         }
        return res;
    }

}
