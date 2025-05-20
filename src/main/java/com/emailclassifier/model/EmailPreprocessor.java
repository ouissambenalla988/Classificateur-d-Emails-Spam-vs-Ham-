package com.emailclassifier.model;

import opennlp.tools.tokenize.SimpleTokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Preprocesses email content for classification by cleaning text,
 * removing noise, and extracting features.
 */
public class EmailPreprocessor {
    private static final Logger logger = LogManager.getLogger(EmailPreprocessor.class);

    private static final Pattern EMAIL_HEADER_PATTERN = Pattern.compile(
        "^(From|To|Subject|Date|Received|CC|BCC|Reply-To|Sender|X-[^:]+):\\s*.*$",
        Pattern.MULTILINE | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+|www\\.\\S+");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}");
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[^a-zA-Z0-9$%!?.]");

    private final Set<String> stopWords;
    private final SimpleTokenizer tokenizer;

    public EmailPreprocessor() {
        this.tokenizer = SimpleTokenizer.INSTANCE;
        this.stopWords = initializeStopWords();
    }

    private Set<String> initializeStopWords() {
        String[] words = {
            "a", "an", "the", "and", "or", "but", "if", "because", "as", "what",
            "which", "this", "that", "these", "those", "then", "just", "so", "than",
            "such", "both", "through", "about", "for", "is", "of", "while", "during",
            "to", "from", "in", "out", "on", "off", "over", "under", "again", "further",
            "once", "here", "there", "when", "where", "why", "how", "all", "any",
            "each", "few", "more", "most", "other", "some", "no", "nor", "not",
            "only", "own", "same", "too", "very", "can", "will", "just", "should",
            "now", "don", "shouldn", "wasn", "aren", "won", "didn", "couldn", "doesn",
            "hasn", "haven", "isn", "mightn", "mustn", "needn", "shan", "wasn", "weren",
            "wouldn", "t", "m", "s", "ll", "d", "re", "ve", "y", "ain", "ma", "o"
        };
        return new HashSet<>(Arrays.asList(words));
    }

    /**
     * Preprocesses email content by removing headers, tags, URLs, and normalizing text.
     *
     * @param emailContent Raw email text
     * @return Cleaned string
     */
    public String preprocess(String emailContent) {
        if (emailContent == null || emailContent.trim().isEmpty()) {
            logger.warn("Email content is null or empty.");
            return "";
        }

        logger.debug("Original email content length: {}", emailContent.length());

        String text = emailContent.toLowerCase();
        text = removeEmailHeaders(text);
        text = HTML_TAG_PATTERN.matcher(text).replaceAll(" ");
        text = URL_PATTERN.matcher(text).replaceAll(" URL_TOKEN ");
        text = EMAIL_PATTERN.matcher(text).replaceAll(" EMAIL_TOKEN ");
        text = SPECIAL_CHAR_PATTERN.matcher(text).replaceAll(" ");
        text = text.replaceAll("\\s+", " ").trim();

        logger.debug("Preprocessed email content: {}", text);
        return text;
    }

    /**
     * Removes common email headers from the text.
     */
    private String removeEmailHeaders(String text) {
        return EMAIL_HEADER_PATTERN.matcher(text).replaceAll("");
    }

    /**
     * Tokenizes text and removes stop words and short tokens.
     *
     * @param text Cleaned email text
     * @return Array of useful tokens
     */
    public String[] tokenize(String text) {
        if (text == null || text.trim().isEmpty()) {
            logger.warn("Cannot tokenize empty text.");
            return new String[0];
        }

        String[] tokens = tokenizer.tokenize(text);
        List<String> filtered = new ArrayList<>();

        for (String token : tokens) {
            if (token.length() > 1 && !stopWords.contains(token)) {
                filtered.add(token);
            }
        }

        logger.debug("Tokenized into {} useful words.", filtered.size());
        return filtered.toArray(new String[0]);
    }
}
