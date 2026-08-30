
package com.example.anjaniimport.service;

import com.example.anjaniimport.dto.ImportRequest;
import com.example.anjaniimport.dto.ProductRecord;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AnjaniScraperService {

    /*
     * ============================================================
     * REGEX PATTERNS
     * ============================================================
     */

    private static final Pattern CODE_PREFIX =
            Pattern.compile("^(\\d{3,6})\\s+.*$");

    /*
     * Matches:
     *
     * 600 X 600 MM
     * 600x600mm
     * 600 * 600 mm
     * 600 X 1200 MM
     * 300x300mm
     */
    private static final Pattern SIZE_PATTERN =
            Pattern.compile(
                    "(?i)(\\d{2,5}\\s*[xX*]\\s*\\d{2,5}(?:\\s*[xX*]\\s*\\d{2,5})?\\s*mm)"
            );

    /*
     * Flexible size conversion pattern.
     */
    private static final Pattern SIZE_CONVERSION_PATTERN =
            Pattern.compile(
                    "(?i)(\\d+(?:\\.\\d+)?)\\s*[xX*]\\s*(\\d+(?:\\.\\d+)?)\\s*mm"
            );

    private static final String LOAD_MORE_ENDPOINT =
            "aj_more_post.php";

    private final String baseUrl;
    private final String userAgent;
    private final long delayMs;
    private final int configuredMax;

    public AnjaniScraperService(
            @Value("${anjani.base-url}") String baseUrl,
            @Value("${anjani.user-agent}") String userAgent,
            @Value("${anjani.request-delay-ms:150}") long delayMs,
            @Value("${anjani.max-products:200}") int configuredMax) {

        this.baseUrl = baseUrl;
        this.userAgent = userAgent;
        this.delayMs = delayMs;
        this.configuredMax = configuredMax;
    }

    /*
     * ============================================================
     * MAIN ENTRY POINT
     * ============================================================
     */

    public List<ProductRecord> fetch(ImportRequest request)
            throws Exception {

        if (request == null) {
            request = new ImportRequest(
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        int requestedMax =
                request.maxProducts() == null
                        ? configuredMax
                        : request.maxProducts();

        int maxProducts =
                Math.min(
                        configuredMax,
                        requestedMax
                );

        if (maxProducts <= 0) {
            maxProducts = configuredMax;
        }

        String requestedCollection =
                clean(request.collection());

        /*
         * Dynamically resolve collection ID.
         */
        String collectionId =
                resolveCollectionId(requestedCollection);

        if (collectionId.isBlank()) {
            throw new IllegalArgumentException(
                    "Collection not found on Anjani website: "
                            + requestedCollection
            );
        }

        System.out.println();
        System.out.println("========================================");
        System.out.println("ANJANI PRODUCT IMPORT");
        System.out.println("========================================");
        System.out.println(
                "Collection       : " + requestedCollection
        );
        System.out.println(
                "Resolved ID      : " + collectionId
        );
        System.out.println(
                "Requested size   : " + request.size()
        );
        System.out.println(
                "Requested finish : " + request.finish()
        );
        System.out.println(
                "Requested color  : " + request.color()
        );
        System.out.println(
                "Maximum products : " + maxProducts
        );
        System.out.println("========================================");

        /*
         * Discover only listing URLs belonging to collection.
         */
        List<String> listUrls =
                discoverCollectionListingUrls(
                        requestedCollection,
                        collectionId,
                        request.size()
                );

        if (listUrls.isEmpty()) {
            System.out.println(
                    "No collection-specific listing URLs found."
            );
            return Collections.emptyList();
        }

        System.out.println();
        System.out.println(
                "TOTAL COLLECTION LISTINGS: "
                        + listUrls.size()
        );

        LinkedHashMap<String, ProductRecord> result =
                new LinkedHashMap<>();

        for (String listUrl : listUrls) {

            if (result.size() >= maxProducts) {
                break;
            }

            System.out.println();
            System.out.println("----------------------------------------");
            System.out.println("PROCESSING COLLECTION LISTING");
            System.out.println("URL : " + listUrl);
            System.out.println("----------------------------------------");

            try {

                scrapeListingWithLoadMore(
                        listUrl,
                        requestedCollection,
                        collectionId,
                        request,
                        maxProducts,
                        result
                );

            } catch (Exception e) {

                System.err.println(
                        "FAILED LISTING: " + listUrl
                );

                System.err.println(
                        "Reason: " + e.getMessage()
                );
            }
        }

        System.out.println();
        System.out.println("========================================");
        System.out.println(
                "TOTAL PRODUCTS RETURNED: "
                        + result.size()
        );
        System.out.println("========================================");

        return new ArrayList<>(result.values());
    }

    /*
     * ============================================================
     * DYNAMIC COLLECTION NAME -> COLLECTION ID
     * ============================================================
     */

    private String resolveCollectionId(
            String requestedCollection)
            throws Exception {

        if (requestedCollection == null
                || requestedCollection.isBlank()) {
            return "";
        }

        String homeUrl = buildBaseUrl();

        System.out.println();
        System.out.println(
                "Reading collections from homepage: "
                        + homeUrl
        );

        Document homePage =
                connect(homeUrl).get();

        Elements collectionLinks =
                homePage.select(
                        "a[href*='product-collection.php']"
                );

        System.out.println(
                "Collection links found on homepage: "
                        + collectionLinks.size()
        );

        String requestedNormalized =
                normalizeCollectionName(
                        requestedCollection
                );

        /*
         * Exact visible-text match.
         */
        for (Element link : collectionLinks) {

            String text =
                    clean(link.text());

            String href =
                    clean(link.attr("href"));

            if (text.isBlank()
                    || href.isBlank()) {
                continue;
            }

            String normalizedText =
                    normalizeCollectionName(text);

            if (!requestedNormalized.equals(
                    normalizedText
            )) {
                continue;
            }

            String collectionId =
                    extractQueryParameter(
                            absolute(href),
                            "collection"
                    );

            if (collectionId.isBlank()) {
                collectionId =
                        extractQueryParameter(
                                absolute(href),
                                "c_id"
                        );
            }

            if (!collectionId.isBlank()) {

                System.out.println(
                        "COLLECTION MATCH FOUND"
                );

                System.out.println(
                        "  Requested : "
                                + requestedCollection
                );

                System.out.println(
                        "  Website   : "
                                + text
                );

                System.out.println(
                        "  ID        : "
                                + collectionId
                );

                System.out.println(
                        "  URL       : "
                                + absolute(href)
                );

                return collectionId;
            }
        }

        /*
         * Parent/card fallback.
         */
        for (Element link : collectionLinks) {

            String href =
                    clean(link.attr("href"));

            if (href.isBlank()) {
                continue;
            }

            String collectionId =
                    extractQueryParameter(
                            absolute(href),
                            "collection"
                    );

            if (collectionId.isBlank()) {
                collectionId =
                        extractQueryParameter(
                                absolute(href),
                                "c_id"
                        );
            }

            if (collectionId.isBlank()) {
                continue;
            }

            Element current = link;

            for (int level = 0;
                 level < 4 && current != null;
                 level++) {

                String parentText =
                        clean(current.text());

                if (normalizeCollectionName(
                        parentText
                ).contains(
                        requestedNormalized
                )) {

                    System.out.println(
                            "COLLECTION MATCH FOUND IN PARENT"
                    );

                    System.out.println(
                            "  Requested : "
                                    + requestedCollection
                    );

                    System.out.println(
                            "  ID        : "
                                    + collectionId
                    );

                    System.out.println(
                            "  URL       : "
                                    + absolute(href)
                    );

                    return collectionId;
                }

                current = current.parent();
            }
        }

        System.err.println(
                "Collection not found: "
                        + requestedCollection
        );

        return "";
    }

    /*
     * ============================================================
     * DISCOVER COLLECTION-SPECIFIC LISTING URLs
     * ============================================================
     */

    private List<String> discoverCollectionListingUrls(
            String collectionName,
            String collectionId,
            String requestedSize)
            throws Exception {

        LinkedHashSet<String> urls =
                new LinkedHashSet<>();

        String collectionUrl =
                buildCollectionUrl(collectionId);

        System.out.println();
        System.out.println(
                "Collection URL: "
                        + collectionUrl
        );

        Document collectionPage =
                connect(collectionUrl).get();

        Elements allProductLinks =
                collectionPage.select(
                        "a[href*='product-list.php']"
                );

        System.out.println(
                "Candidate product-list links found: "
                        + allProductLinks.size()
        );

        for (Element link : allProductLinks) {

            String href =
                    clean(link.attr("href"));

            if (href.isBlank()) {
                continue;
            }

            /*
             * Only Explore all Products links.
             */
            if (!isExploreProductsLink(link)) {
                continue;
            }

            String cardSize =
                    findCollectionCardSize(link);

            if (requestedSize != null
                    && !requestedSize.isBlank()) {

                if (!matchesSize(
                        requestedSize,
                        cardSize,
                        href
                )) {
                    continue;
                }
            }

            String absoluteUrl =
                    absolute(href);

            if (absoluteUrl.isBlank()) {
                continue;
            }

            if (!absoluteUrl.contains(
                    "product-list.php"
            )) {
                continue;
            }

            urls.add(absoluteUrl);

            System.out.println(
                    "COLLECTION SIZE FOUND:"
            );

            System.out.println(
                    "  Size : " + cardSize
            );

            System.out.println(
                    "  URL  : " + absoluteUrl
            );
        }

        System.out.println();
        System.out.println(
                "Unique collection listing URLs: "
                        + urls.size()
        );

        return new ArrayList<>(urls);
    }

    private boolean isExploreProductsLink(
            Element link) {

        String text =
                normalize(link.text());

        return text.contains(
                "explore all products"
        );
    }

    private String findCollectionCardSize(
            Element link) {

        Element current = link;

        for (int i = 0;
             i < 5 && current != null;
             i++) {

            String text =
                    clean(current.text());

            Matcher matcher =
                    SIZE_PATTERN.matcher(text);

            if (matcher.find()) {

                return clean(
                        matcher.group(1)
                );
            }

            current = current.parent();
        }

        return extractDisplaySizeFromListingUrl(
                link.attr("href")
        );
    }

    private String extractDisplaySizeFromListingUrl(
            String url) {

        if (url == null || url.isBlank()) {
            return "";
        }

        Matcher matcher =
                SIZE_PATTERN.matcher(
                        url.replace(
                                "%20",
                                " "
                        )
                );

        if (matcher.find()) {

            return clean(
                    matcher.group(1)
            );
        }

        return "";
    }

    /*
     * ============================================================
     * SIZE FILTER
     * ============================================================
     */

    private boolean matchesSize(
            String requestedSize,
            String cardSize,
            String listingUrl) {

        if (requestedSize == null
                || requestedSize.isBlank()) {
            return true;
        }

        if (cardSize != null
                && !cardSize.isBlank()) {

            if (matchesValue(
                    requestedSize,
                    cardSize
            )) {
                return true;
            }

            String converted =
                    convertSizeToFeet(cardSize);

            if (!converted.isBlank()
                    && matchesValue(
                    requestedSize,
                    converted
            )) {
                return true;
            }
        }

        String numericRequested =
                requestedSize.trim();

        if (numericRequested.matches("\\d+")) {

            String sizeId =
                    extractQueryParameter(
                            listingUrl,
                            "size"
                    );

            return numericRequested.equals(
                    sizeId
            );
        }

        return false;
    }

    /*
     * ============================================================
     * SCRAPE LISTING + LOAD MORE
     * ============================================================
     */

    private void scrapeListingWithLoadMore(
            String listUrl,
            String expectedCollection,
            String expectedCollectionId,
            ImportRequest request,
            int maxProducts,
            LinkedHashMap<String, ProductRecord> result)
            throws Exception {

        Document currentPage =
                connect(listUrl).get();

        String cid =
                extractHiddenValue(
                        currentPage,
                        "c_id"
                );

        String sid =
                extractHiddenValue(
                        currentPage,
                        "s_id"
                );

        if (cid.isBlank()) {

            cid =
                    extractCollectionIdForAjax(
                            currentPage
                    );
        }

        if (sid.isBlank()) {

            sid =
                    extractSizeIdForAjax(
                            currentPage
                    );
        }

        System.out.println(
                "Listing AJAX c_id = " + cid
        );

        System.out.println(
                "Listing AJAX s_id = " + sid
        );

        int before =
                result.size();

        addProductsFromDocument(
                currentPage,
                listUrl,
                expectedCollection,
                request,
                maxProducts,
                result
        );

        System.out.println(
                "Products added from listing = "
                        + (result.size() - before)
        );

        if (result.size() >= maxProducts) {
            return;
        }

        Set<String> processedProdIds =
                new HashSet<>();

        int loadMoreCount = 0;

        while (result.size() < maxProducts) {

            String prodId =
                    extractLoadMoreProductId(
                            currentPage
                    );

            if (prodId.isBlank()) {

                System.out.println(
                        "No more Load More button."
                );

                break;
            }

            if (!processedProdIds.add(prodId)) {

                System.out.println(
                        "Same prod_id encountered again: "
                                + prodId
                );

                break;
            }

            loadMoreCount++;

            System.out.println();
            System.out.println(
                    "LOAD MORE #" + loadMoreCount
            );

            System.out.println(
                    "prod_id = " + prodId
            );

            System.out.println(
                    "cid = " + cid
            );

            System.out.println(
                    "sid = " + sid
            );

            Document morePage =
                    callLoadMore(
                            prodId,
                            cid,
                            sid,
                            listUrl
                    );

            if (morePage == null) {

                System.out.println(
                        "Load More returned no response."
                );

                break;
            }

            String responseText =
                    clean(
                            morePage.body().text()
                    );

            if (responseText.isBlank()) {

                System.out.println(
                        "Load More returned empty content."
                );

                break;
            }

            System.out.println(
                    "AJAX response received."
            );

            int productsBefore =
                    result.size();

            addProductsFromDocument(
                    morePage,
                    listUrl,
                    expectedCollection,
                    request,
                    maxProducts,
                    result
            );

            int productsAdded =
                    result.size()
                            - productsBefore;

            System.out.println(
                    "Products added = "
                            + productsAdded
            );

            currentPage =
                    morePage;

            String nextProdId =
                    extractLoadMoreProductId(
                            currentPage
                    );

            if (nextProdId.isBlank()) {

                System.out.println(
                        "No next Load More button."
                );

                break;
            }

            sleep();
        }

        System.out.println(
                "Load More calls = "
                        + loadMoreCount
        );
    }

    /*
     * ============================================================
     * AJAX LOAD MORE
     * ============================================================
     */

    private Document callLoadMore(
            String prodId,
            String cid,
            String sid,
            String refererUrl)
            throws Exception {

        String endpoint =
                buildBaseUrl()
                        + LOAD_MORE_ENDPOINT;

        System.out.println(
                "AJAX URL = " + endpoint
        );

        Connection connection =
                Jsoup.connect(endpoint)
                        .userAgent(userAgent)
                        .timeout(30000)
                        .method(Connection.Method.POST)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(false)
                        .followRedirects(true)
                        .header(
                                "X-Requested-With",
                                "XMLHttpRequest"
                        )
                        .header(
                                "Accept",
                                "*/*"
                        )
                        .header(
                                "Referer",
                                refererUrl == null
                                        ? buildBaseUrl()
                                        : refererUrl
                        )
                        .data(
                                "prod_id",
                                prodId == null
                                        ? ""
                                        : prodId
                        )
                        .data(
                                "cid",
                                cid == null
                                        ? ""
                                        : cid
                        )
                        .data(
                                "sid",
                                sid == null
                                        ? ""
                                        : sid
                        );

        String html =
                connection.execute().body();

        if (html == null
                || html.isBlank()) {
            return null;
        }

        System.out.println(
                "AJAX response length = "
                        + html.length()
        );

        return Jsoup.parse(
                html,
                buildBaseUrl()
        );
    }

    /*
     * ============================================================
     * ADD PRODUCTS
     * ============================================================
     */

    private void addProductsFromDocument(
            Document document,
            String sourceUrl,
            String expectedCollection,
            ImportRequest request,
            int maxProducts,
            LinkedHashMap<String, ProductRecord> result) {

        Elements links =
                document.select(
                        "a[href*='product-detail.php']"
                );

        System.out.println(
                "Product links found = "
                        + links.size()
        );

        LinkedHashMap<String, Element> uniqueLinks =
                new LinkedHashMap<>();

        for (Element link : links) {

            String href =
                    clean(link.attr("href"));

            if (href.isBlank()) {
                continue;
            }

            String detailUrl =
                    absolute(href);

            if (detailUrl.isBlank()) {
                continue;
            }

            if (!detailUrl.contains(
                    "product-detail.php"
            )) {
                continue;
            }

            int hash =
                    detailUrl.indexOf('#');

            if (hash >= 0) {

                detailUrl =
                        detailUrl.substring(
                                0,
                                hash
                        );
            }

            uniqueLinks.putIfAbsent(
                    detailUrl,
                    link
            );
        }

        for (Map.Entry<String, Element> entry
                : uniqueLinks.entrySet()) {

            if (result.size() >= maxProducts) {
                break;
            }

            String detailUrl =
                    entry.getKey();

            Element link =
                    entry.getValue();

            /*
             * IMPORTANT:
             *
             * Do not use link.text() as the main product name.
             *
             * In the current Anjani HTML the link may contain
             * text such as "img".
             *
             * The real product name is in:
             *
             * <img alt="Blue Cube (VNR3300004C)">
             *
             * So parseDetail() will extract the IMG ALT.
             */

            String fallbackName =
                    extractProductNameFromListingImage(
                            link
                    );

            if (fallbackName.isBlank()) {

                fallbackName =
                        clean(link.text());

                if (fallbackName.equalsIgnoreCase(
                        "Product Detail"
                )) {
                    fallbackName = "";
                }

                if (fallbackName.equalsIgnoreCase(
                        "img"
                )) {
                    fallbackName = "";
                }
            }

            try {

                /*
                 * Open actual product detail page.
                 */
                Document detail =
                        connect(detailUrl).get();

                ProductRecord product =
                        parseDetail(
                                detail,
                                detailUrl,
                                fallbackName,
                                sourceUrl
                        );

                if (product.name() == null
                        || product.name().isBlank()) {

                    System.out.println(
                            "SKIPPED: Empty product name"
                    );

                    continue;
                }

                /*
                 * Final collection validation.
                 */
                if (!matchesCollection(
                        expectedCollection,
                        product.collection()
                )) {

                    System.out.println(
                            "WRONG COLLECTION - SKIPPED: "
                                    + product.name()
                                    + " | collection="
                                    + product.collection()
                    );

                    continue;
                }

                /*
                 * Optional filters.
                 */
                if (!matchesOptionalFilters(
                        product,
                        request
                )) {

                    System.out.println(
                            "FILTER MISMATCH: "
                                    + product.name()
                    );

                    continue;
                }

                if (!result.containsKey(
                        product.importKey()
                )) {

                    result.put(
                            product.importKey(),
                            product
                    );

                    System.out.println(
                            "PRODUCT ADDED: "
                                    + product.name()
                    );

                    System.out.println(
                            "  Collection : "
                                    + product.collection()
                    );

                    System.out.println(
                            "  Size       : "
                                    + product.size()
                    );

                    System.out.println(
                            "  Finish     : "
                                    + product.finish()
                    );

                    System.out.println(
                            "  Color      : "
                                    + product.color()
                    );

                    System.out.println(
                            "  Application: "
                                    + product.application()
                    );

                    System.out.println(
                            "  Code       : "
                                    + product.supplierProductCode()
                    );

                    System.out.println(
                            "  Image      : "
                                    + product.imageUrl()
                    );

                    System.out.println(
                            "  Import Key : "
                                    + product.importKey()
                    );

                } else {

                    System.out.println(
                            "DUPLICATE SKIPPED: "
                                    + product.name()
                    );
                }

            } catch (Exception e) {

                System.err.println(
                        "FAILED PRODUCT: "
                                + detailUrl
                );

                System.err.println(
                        "Reason: "
                                + e.getMessage()
                );
            }

            sleep();
        }
    }

    /*
     * ============================================================
     * PRODUCT NAME FROM LISTING IMAGE
     * ============================================================
     *
     * Handles:
     *
     * <a href="product-detail.php?...">
     *
     *     <img
     *         src="uploads/products/..."
     *         alt="Blue Cube (VNR3300004C)"
     *     >
     *
     * </a>
     *
     * ============================================================
     */

    private String extractProductNameFromListingImage(
            Element link) {

        if (link == null) {
            return "";
        }

        /*
         * First priority:
         *
         * IMG inside product-detail link.
         */
        Element image =
                link.selectFirst("img[alt]");

        if (image != null) {

            String alt =
                    clean(image.attr("alt"));

            if (isValidProductName(alt)) {

                System.out.println(
                        "PRODUCT NAME FOUND FROM <img alt>: "
                                + alt
                );

                return alt;
            }
        }

        /*
         * Sometimes the image may be in the parent
         * product card instead of directly inside <a>.
         */
        Element current =
                link.parent();

        for (int i = 0;
             i < 3 && current != null;
             i++) {

            Elements images =
                    current.select("img[alt]");

            for (Element img : images) {

                String alt =
                        clean(img.attr("alt"));

                if (isValidProductName(alt)) {

                    System.out.println(
                            "PRODUCT NAME FOUND FROM CARD <img alt>: "
                                    + alt
                    );

                    return alt;
                }
            }

            current =
                    current.parent();
        }

        return "";
    }

    /*
     * ============================================================
     * COLLECTION MATCH
     * ============================================================
     */

    private boolean matchesCollection(
            String expectedCollection,
            String actualCollection) {

        if (expectedCollection == null
                || expectedCollection.isBlank()) {
            return true;
        }

        if (actualCollection == null
                || actualCollection.isBlank()) {
            return false;
        }

        String expected =
                normalizeCollectionName(
                        expectedCollection
                );

        String actual =
                normalizeCollectionName(
                        actualCollection
                );

        return expected.equals(actual);
    }

    private String normalizeCollectionName(
            String value) {

        String normalized =
                normalize(value);

        return switch (normalized) {

            case "wall",
                 "wall collection",
                 "wall collections",
                 "wall tiles" ->
                    "wall collections";

            case "gvt",
                 "gvt collection",
                 "gvt collections",
                 "gvt tiles" ->
                    "gvt collections";

            case "parking",
                 "parking tile",
                 "parking tiles" ->
                    "parking tiles";

            case "cool roof",
                 "cool roof tile",
                 "cool roof tiles" ->
                    "cool roof";

            case "sparkel",
                 "sparkel tile",
                 "sparkel tiles" ->
                    "sparkel tiles";

            case "step riser",
                 "step riser tile",
                 "step riser tiles",
                 "step and riser tiles" ->
                    "step riser tiles";

            default ->
                    normalized;
        };
    }

    /*
     * ============================================================
     * PARSE PRODUCT DETAIL
     * ============================================================
     */

    private ProductRecord parseDetail(
            Document document,
            String detailUrl,
            String fallbackName,
            String sourceUrl) {

        /*
         * IMPORTANT:
         *
         * Product name priority:
         *
         * 1. IMG ALT
         * 2. H1
         * 3. H2
         * 4. Product title selectors
         * 5. OG title
         * 6. Meta title
         * 7. Document title
         * 8. URL
         * 9. Listing fallback
         */

        String name =
                extractProductName(
                        document,
                        detailUrl,
                        fallbackName
                );

        String bodyText =
                clean(
                        document.body() == null
                                ? ""
                                : document.body().text()
                );

        String collection =
                findValueByLabel(
                        document,
                        "Collection"
                );

        String size =
                findValueByLabel(
                        document,
                        "Size"
                );

        String finish =
                findValueByLabel(
                        document,
                        "Finish"
                );

        String color =
                findValueByLabel(
                        document,
                        "Color"
                );

        String application =
                findValueByLabel(
                        document,
                        "Application"
                );

        /*
         * Body text fallbacks.
         */
        if (collection.isBlank()) {

            collection =
                    labelValue(
                            bodyText,
                            "Collection"
                    );
        }

        if (size.isBlank()) {

            size =
                    labelValue(
                            bodyText,
                            "Size"
                    );
        }

        if (finish.isBlank()) {

            finish =
                    labelValue(
                            bodyText,
                            "Finish"
                    );
        }

        if (color.isBlank()) {

            color =
                    labelValue(
                            bodyText,
                            "Color"
                    );
        }

        if (application.isBlank()) {

            application =
                    labelValue(
                            bodyText,
                            "Application"
                    );
        }

        /*
         * ========================================================
         * NEW ANJANI HTML STRUCTURE
         * ========================================================
         *
         * Example:
         *
         * <img
         *   src="uploads/products/..."
         *   alt="Blue Cube (VNR3300004C)"
         * >
         *
         * <p>300x300mm (Roof)</p>
         *
         * Extract size and application directly from <p>.
         */
        if (size.isBlank()
                || application.isBlank()) {

            for (Element paragraph :
                    document.select("p")) {

                String text =
                        clean(paragraph.text());

                if (text.isBlank()) {
                    continue;
                }

                Matcher matcher =
                        SIZE_PATTERN.matcher(text);

                if (matcher.find()) {

                    if (size.isBlank()) {

                        size =
                                clean(
                                        matcher.group(1)
                                );

                        System.out.println(
                                "PRODUCT SIZE FOUND FROM <p>: "
                                        + size
                        );
                    }

                    /*
                     * Text after the size:
                     *
                     * 300x300mm (Roof)
                     *
                     * becomes:
                     *
                     * Roof
                     */
                    String applicationValue =
                            extractApplicationFromParagraph(
                                    text,
                                    matcher.group(1)
                            );

                    if (application.isBlank()
                            && !applicationValue.isBlank()) {

                        application =
                                applicationValue;

                        System.out.println(
                                "PRODUCT APPLICATION FOUND FROM <p>: "
                                        + application
                        );
                    }
                }
            }
        }

        /*
         * Search complete body for physical size.
         */
        if (size.isBlank()) {

            Matcher matcher =
                    SIZE_PATTERN.matcher(
                            bodyText
                    );

            if (matcher.find()) {

                size =
                        clean(
                                matcher.group(1)
                        );
            }
        }

        /*
         * Try size from URL / page.
         */
        if (size.isBlank()) {

            String sizeId =
                    extractQueryParameter(
                            detailUrl,
                            "size"
                    );

            if (!sizeId.isBlank()) {

                size =
                        resolveSizeFromDocument(
                                document,
                                sizeId
                        );
            }
        }

        /*
         * Additional size fallback.
         */
        if (size.isBlank()) {

            size =
                    extractSizeFromPage(
                            document
                    );
        }

        /*
         * Convert:
         *
         * 300 X 300 MM
         *       ↓
         * 1 X 1
         *
         * 600 X 600 MM
         *       ↓
         * 2 X 2
         *
         * 600 X 1200 MM
         *       ↓
         * 2 X 4
         */
        String displaySize =
                convertSizeToFeet(size);

        if (!displaySize.isBlank()) {

            size =
                    displaySize;
        }

        /*
         * Image.
         */
        String imageUrl =
                extractBestImage(
                        document,
                        name
                );

        /*
         * Product code.
         */
        String code =
                extractCode(name);

        if (code.isBlank()) {

            code =
                    extractCodeFromUrl(
                            detailUrl
                    );
        }

        /*
         * Import key.
         */
        String importKey =
                buildImportKey(
                        detailUrl,
                        code,
                        name,
                        size
                );

        /*
         * IMPORTANT:
         *
         * Supplier changed from:
         *
         * ANJANI_TEK
         *
         * to:
         *
         * ANJANI
         */
        return new ProductRecord(
                "ANJANI",
                code,
                name,
                collection,
                size,
                finish,
                color,
                application,
                detailUrl,
                imageUrl,
                sourceUrl,
                importKey
        );
    }

    /*
     * ============================================================
     * EXTRACT APPLICATION FROM <p>
     * ============================================================
     *
     * Example:
     *
     * 300x300mm (Roof)
     *
     * Result:
     *
     * Roof
     *
     * ============================================================
     */

    private String extractApplicationFromParagraph(
            String paragraph,
            String matchedSize) {

        if (paragraph == null
                || paragraph.isBlank()) {
            return "";
        }

        String value =
                paragraph;

        if (matchedSize != null
                && !matchedSize.isBlank()) {

            value =
                    value.replaceFirst(
                            "(?i)"
                                    + Pattern.quote(
                                    matchedSize
                            ),
                            ""
                    );
        }

        value =
                clean(value);

        /*
         * Remove surrounding brackets.
         */
        value =
                value.replaceAll(
                        "^\\s*[\\(\\[\\{]\\s*",
                        ""
                );

        value =
                value.replaceAll(
                        "\\s*[\\)\\]\\}]\\s*$",
                        ""
                );

        return clean(value);
    }

    /*
     * ============================================================
     * PRODUCT NAME
     * ============================================================
     */

    private String extractProductName(
            Document document,
            String detailUrl,
            String fallbackName) {

        /*
         * ========================================================
         * 1. HIGHEST PRIORITY - PRODUCT IMAGE ALT
         * ========================================================
         *
         * This is the important fix.
         *
         * Example:
         *
         * <img
         *   src="uploads/products/..."
         *   alt="Blue Cube (VNR3300004C)"
         * >
         *
         * We return:
         *
         * Blue Cube (VNR3300004C)
         */

        String imageAltName =
                extractProductNameFromImageAlt(
                        document
                );

        if (!imageAltName.isBlank()) {

            System.out.println(
                    "PRODUCT NAME FOUND FROM IMG ALT: "
                            + imageAltName
            );

            return imageAltName;
        }

        /*
         * 2. H1
         */
        Element h1 =
                document.selectFirst("h1");

        if (h1 != null) {

            String value =
                    clean(h1.text());

            if (isValidProductName(value)) {
                return value;
            }
        }

        /*
         * 3. H2
         */
        Element h2 =
                document.selectFirst("h2");

        if (h2 != null) {

            String value =
                    clean(h2.text());

            if (isValidProductName(value)) {
                return value;
            }
        }

        /*
         * 4. Product-specific selectors.
         */
        String[] selectors = {

                ".product-title",
                ".product-name",
                ".product-details-title",
                ".product-detail-title",
                ".product-details__title",
                ".tp-product-details-title",
                ".tp-product-details__title",
                ".single-product-title",
                ".single-product .product-title",
                ".product-detail h2",
                ".product-details h2",
                ".single-product h2",
                ".product-detail h3",
                ".product-details h3"
        };

        for (String selector : selectors) {

            Element element =
                    document.selectFirst(
                            selector
                    );

            if (element == null) {
                continue;
            }

            String value =
                    clean(element.text());

            if (isValidProductName(value)) {
                return value;
            }
        }

        /*
         * 5. OpenGraph title.
         */
        Element ogTitle =
                document.selectFirst(
                        "meta[property='og:title']"
                );

        if (ogTitle != null) {

            String value =
                    clean(
                            ogTitle.attr("content")
                    );

            if (isValidProductName(value)) {

                return removeWebsiteSuffix(
                        value
                );
            }
        }

        /*
         * 6. Meta title.
         */
        Element metaTitle =
                document.selectFirst(
                        "meta[name='title']"
                );

        if (metaTitle != null) {

            String value =
                    clean(
                            metaTitle.attr("content")
                    );

            if (isValidProductName(value)) {

                return removeWebsiteSuffix(
                        value
                );
            }
        }

        /*
         * 7. HTML document title.
         */
        String title =
                clean(document.title());

        if (isValidProductName(title)) {

            title =
                    removeWebsiteSuffix(
                            title
                    );

            if (isValidProductName(title)) {
                return title;
            }
        }

        /*
         * 8. Product name from URL.
         */
        String urlName =
                extractProductNameFromUrl(
                        detailUrl
                );

        if (!urlName.isBlank()) {
            return urlName;
        }

        /*
         * 9. Listing fallback.
         */
        if (isValidProductName(
                fallbackName
        )) {

            return clean(fallbackName);
        }

        return "";
    }

    /*
     * ============================================================
     * PRODUCT NAME FROM IMAGE ALT
     * ============================================================
     */

    private String extractProductNameFromImageAlt(
            Document document) {

        if (document == null) {
            return "";
        }

        /*
         * First priority:
         *
         * Product images under:
         *
         * /uploads/products/
         */
        for (Element image :
                document.select(
                        "img[alt][src*='uploads/products']"
                )) {

            String alt =
                    clean(
                            image.attr("alt")
                    );

            if (isValidProductName(alt)) {

                return alt;
            }
        }

        /*
         * Try lazy-loaded product images too.
         */
        for (Element image :
                document.select("img[alt]")) {

            String alt =
                    clean(
                            image.attr("alt")
                    );

            if (!isValidProductName(alt)) {
                continue;
            }

            String src =
                    getImageSource(image);

            String lowerSrc =
                    src.toLowerCase(
                            Locale.ROOT
                    );

            if (lowerSrc.contains(
                    "/uploads/products/"
            )) {

                return alt;
            }
        }

        /*
         * Last IMG ALT fallback.
         */
        for (Element image :
                document.select("img[alt]")) {

            String alt =
                    clean(
                            image.attr("alt")
                    );

            if (isValidProductName(alt)) {
                return alt;
            }
        }

        return "";
    }

    /*
     * ============================================================
     * VALID PRODUCT NAME
     * ============================================================
     */

    private boolean isValidProductName(
            String value) {

        if (value == null
                || value.isBlank()) {
            return false;
        }

        String normalized =
                normalize(value);

        if (normalized.equals(
                "product detail"
        )
                || normalized.equals(
                "product details"
        )
                || normalized.equals(
                "anjani tek"
        )
                || normalized.equals(
                "anjani tiles"
        )
                || normalized.equals(
                "tiles"
        )
                || normalized.equals(
                "img"
        )
                || normalized.equals(
                "image"
        )
                || normalized.equals(
                "view"
        )
                || normalized.equals(
                "view product"
        )) {

            return false;
        }

        return true;
    }

    /*
     * ============================================================
     * PRODUCT NAME FROM URL
     * ============================================================
     */

    private String extractProductNameFromUrl(
            String detailUrl) {

        if (detailUrl == null
                || detailUrl.isBlank()) {
            return "";
        }

        String id =
                extractQueryParameter(
                        detailUrl,
                        "id"
                );

        if (id.isBlank()) {
            return "";
        }

        try {

            String decoded =
                    URLDecoder.decode(
                            id,
                            StandardCharsets.UTF_8
                    );

            decoded =
                    decoded.replace(
                            "-",
                            " "
                    );

            return clean(decoded);

        } catch (Exception e) {

            return clean(
                    id.replace(
                            "-",
                            " "
                    )
            );
        }
    }

    /*
     * ============================================================
     * REMOVE WEBSITE TITLE SUFFIX
     * ============================================================
     */

    private String removeWebsiteSuffix(
            String title) {

        if (title == null
                || title.isBlank()) {
            return "";
        }

        String result = title;

        String[] separators = {

                " | ",
                " - Anjani",
                " | Anjani",
                " - Anjani Tiles",
                " | Anjani Tiles",
                " - Anjani Tiles & Marbles"
        };

        for (String separator :
                separators) {

            int index =
                    result.toLowerCase(
                            Locale.ROOT
                    ).indexOf(
                            separator.toLowerCase(
                                    Locale.ROOT
                            )
                    );

            if (index > 0) {

                result =
                        result.substring(
                                0,
                                index
                        );

                break;
            }
        }

        return clean(result);
    }

    /*
     * ============================================================
     * RESOLVE SIZE FROM DOCUMENT
     * ============================================================
     */

    private String resolveSizeFromDocument(
            Document document,
            String sizeId) {

        if (document == null
                || sizeId == null
                || sizeId.isBlank()) {
            return "";
        }

        for (Element element :
                document.select("*")) {

            String text =
                    clean(element.text());

            if (text.isBlank()) {
                continue;
            }

            Matcher matcher =
                    SIZE_PATTERN.matcher(text);

            if (matcher.find()) {

                return clean(
                        matcher.group(1)
                );
            }
        }

        return "";
    }

    /*
     * ============================================================
     * EXTRACT SIZE FROM PRODUCT PAGE
     * ============================================================
     */

    private String extractSizeFromPage(
            Document document) {

        if (document == null) {
            return "";
        }

        String bodyText =
                clean(
                        document.body() == null
                                ? ""
                                : document.body().text()
                );

        Matcher matcher =
                SIZE_PATTERN.matcher(
                        bodyText
                );

        if (matcher.find()) {

            return clean(
                    matcher.group(1)
            );
        }

        /*
         * Search data attributes.
         */
        for (Element element :
                document.select(
                        "[data-size], "
                                + "[data-value], "
                                + "[value]"
                )) {

            String[] attributes = {

                    "data-size",
                    "data-value",
                    "value"
            };

            for (String attribute :
                    attributes) {

                String value =
                        clean(
                                element.attr(
                                        attribute
                                )
                        );

                if (value.isBlank()) {
                    continue;
                }

                Matcher sizeMatcher =
                        SIZE_PATTERN.matcher(
                                value
                        );

                if (sizeMatcher.find()) {

                    return clean(
                            sizeMatcher.group(1)
                    );
                }
            }
        }

        return "";
    }

    /*
     * ============================================================
     * CONVERT SIZE TO FEET
     * ============================================================
     */

    private String convertSizeToFeet(
            String size) {

        if (size == null
                || size.isBlank()) {
            return "";
        }

        Matcher matcher =
                SIZE_CONVERSION_PATTERN.matcher(
                        size
                );

        if (!matcher.find()) {
            return "";
        }

        try {

            double widthMm =
                    Double.parseDouble(
                            matcher.group(1)
                    );

            double heightMm =
                    Double.parseDouble(
                            matcher.group(2)
                    );

            /*
             * 1 foot = 304.8 mm
             */
            double widthFeet =
                    widthMm / 304.8;

            double heightFeet =
                    heightMm / 304.8;

            String width =
                    formatFeetSize(
                            widthFeet
                    );

            String height =
                    formatFeetSize(
                            heightFeet
                    );

            return width
                    + " X "
                    + height;

        } catch (Exception e) {

            return "";
        }
    }

    /*
     * ============================================================
     * FORMAT FEET
     * ============================================================
     */

    private String formatFeetSize(
            double feet) {

        double rounded =
                Math.round(feet);

        if (Math.abs(
                feet - rounded
        ) < 0.05) {

            return String.valueOf(
                    (long) rounded
            );
        }

        return String.format(
                Locale.ROOT,
                "%.2f",
                feet
        );
    }

    /*
     * ============================================================
     * FIND TABLE LABEL/VALUE
     * ============================================================
     */

    private String findValueByLabel(
            Document document,
            String label) {

        /*
         * TABLES
         */
        for (Element row :
                document.select("tr")) {

            Elements cells =
                    row.select("th, td");

            if (cells.size() >= 2) {

                String first =
                        clean(
                                cells.get(0).text()
                        );

                if (first.equalsIgnoreCase(
                        label
                )) {

                    return clean(
                            cells.get(
                                    cells.size() - 1
                            ).text()
                    );
                }
            }
        }

        /*
         * NORMAL ELEMENT + SIBLING
         */
        for (Element element :
                document.select("*")) {

            String text =
                    clean(
                            element.text()
                    );

            if (!text.equalsIgnoreCase(
                    label
            )) {
                continue;
            }

            Element sibling =
                    element.nextElementSibling();

            if (sibling != null) {

                String value =
                        clean(
                                sibling.text()
                        );

                if (!value.isBlank()
                        && !value.equalsIgnoreCase(
                        label
                )) {

                    return value;
                }
            }

            /*
             * Parent children.
             */
            Element parent =
                    element.parent();

            if (parent != null) {

                Elements children =
                        parent.children();

                int index =
                        children.indexOf(
                                element
                        );

                if (index >= 0
                        && index + 1
                        < children.size()) {

                    String value =
                            clean(
                                    children.get(
                                            index + 1
                                    ).text()
                            );

                    if (!value.isBlank()
                            && !value.equalsIgnoreCase(
                            label
                    )) {

                        return value;
                    }
                }
            }
        }

        return "";
    }

    /*
     * ============================================================
     * BODY TEXT LABEL PARSER
     * ============================================================
     */

    private String labelValue(
            String text,
            String label) {

        if (text == null
                || text.isBlank()) {
            return "";
        }

        String[] labels = {

                "Collection",
                "Size",
                "Finish",
                "Color",
                "Application"
        };

        StringBuilder nextLabels =
                new StringBuilder();

        for (String current :
                labels) {

            if (!current.equalsIgnoreCase(
                    label
            )) {

                if (nextLabels.length() > 0) {
                    nextLabels.append("|");
                }

                nextLabels.append(
                        Pattern.quote(current)
                );
            }
        }

        String regex =
                "(?i)"
                        + Pattern.quote(label)
                        + "\\s*[:|]?\\s*"
                        + "(.{1,120}?)"
                        + "(?=\\s+(?:"
                        + nextLabels
                        + ")\\b|$)";

        Matcher matcher =
                Pattern.compile(regex)
                        .matcher(text);

        if (matcher.find()) {

            return clean(
                    matcher.group(1)
            );
        }

        return "";
    }

    /*
     * ============================================================
     * IMAGE EXTRACTION
     * ============================================================
     */

    private String extractBestImage(
            Document document,
            String productName) {

        String normalizedName =
                normalize(productName);

        /*
         * FIRST PRIORITY:
         *
         * /uploads/products/
         */
        for (Element image :
                document.select("img")) {

            String src =
                    getImageSource(image);

            if (src.isBlank()) {
                continue;
            }

            String lowerSrc =
                    src.toLowerCase(
                            Locale.ROOT
                    );

            if (lowerSrc.contains(
                    "/uploads/products/"
            )
                    && !isInvalidImage(
                    lowerSrc
            )) {

                return src;
            }
        }

        /*
         * Product-specific containers.
         */
        Elements productImages =
                document.select(
                        ".product img, "
                                + ".product-detail img, "
                                + ".product-details img, "
                                + ".single-product img, "
                                + ".item img"
                );

        for (Element image :
                productImages) {

            String src =
                    getImageSource(image);

            if (src.isBlank()) {
                continue;
            }

            String lowerSrc =
                    src.toLowerCase(
                            Locale.ROOT
                    );

            if (isInvalidImage(
                    lowerSrc
            )) {
                continue;
            }

            return src;
        }

        /*
         * Match image alt with product name.
         */
        for (Element image :
                document.select("img")) {

            String src =
                    getImageSource(image);

            if (src.isBlank()) {
                continue;
            }

            String lowerSrc =
                    src.toLowerCase(
                            Locale.ROOT
                    );

            if (isInvalidImage(
                    lowerSrc
            )) {
                continue;
            }

            String alt =
                    normalize(
                            image.attr("alt")
                    );

            if (!alt.isBlank()
                    && !normalizedName.isBlank()
                    && (
                    alt.contains(
                            normalizedName
                    )
                            || normalizedName.contains(
                            alt
                    )
            )) {

                return src;
            }
        }

        /*
         * Last valid image.
         */
        for (Element image :
                document.select("img")) {

            String src =
                    getImageSource(image);

            if (src.isBlank()) {
                continue;
            }

            String lowerSrc =
                    src.toLowerCase(
                            Locale.ROOT
                    );

            if (!isInvalidImage(
                    lowerSrc
            )) {

                return src;
            }
        }

        return "";
    }

    private boolean isInvalidImage(
            String src) {

        if (src == null
                || src.isBlank()) {
            return true;
        }

        String value =
                src.toLowerCase(
                        Locale.ROOT
                );

        return value.contains("/logo/")
                || value.contains("logo.")
                || value.contains("/favicon")
                || value.contains("favicon.")
                || value.contains("/icon/")
                || value.contains("icon.")
                || value.contains("facebook")
                || value.contains("instagram")
                || value.contains("twitter")
                || value.contains("linkedin");
    }

    private String getImageSource(
            Element image) {

        String[] attributes = {

                "src",
                "data-src",
                "data-lazy-src",
                "data-original",
                "data-image"
        };

        for (String attribute :
                attributes) {

            String value =
                    image.attr(attribute);

            if (value == null
                    || value.isBlank()) {
                continue;
            }

            String absoluteUrl =
                    image.absUrl(attribute);

            if (!absoluteUrl.isBlank()) {
                return absoluteUrl;
            }

            return absolute(value);
        }

        return "";
    }

    /*
     * ============================================================
     * OPTIONAL FILTERS
     * ============================================================
     */

    private boolean matchesOptionalFilters(
            ProductRecord product,
            ImportRequest request) {

        if (request.size() != null
                && !request.size().isBlank()) {

            if (!matchesValue(
                    request.size(),
                    product.size()
            )) {

                String converted =
                        convertSizeToFeet(
                                request.size()
                        );

                if (converted.isBlank()
                        || !matchesValue(
                        converted,
                        product.size()
                )) {

                    return false;
                }
            }
        }

        if (request.finish() != null
                && !request.finish().isBlank()) {

            if (!matchesValue(
                    request.finish(),
                    product.finish()
            )) {

                return false;
            }
        }

        if (request.color() != null
                && !request.color().isBlank()) {

            if (!matchesValue(
                    request.color(),
                    product.color()
            )) {

                return false;
            }
        }

        return true;
    }

    private boolean matchesValue(
            String filter,
            String actualValue) {

        if (filter == null
                || filter.isBlank()) {
            return true;
        }

        if (actualValue == null
                || actualValue.isBlank()) {
            return false;
        }

        return normalize(actualValue)
                .contains(
                        normalize(filter)
                );
    }

    /*
     * ============================================================
     * PRODUCT CODE
     * ============================================================
     */

    private String extractCode(
            String name) {

        if (name == null
                || name.isBlank()) {
            return "";
        }

        String value =
                clean(name);

        /*
         * 1. Code inside parentheses.
         *
         * Blue Cube (VNR3300004C)
         */
        Pattern bracketCode =
                Pattern.compile(
                        "\\(([A-Za-z0-9]+)\\)"
                );

        Matcher matcher =
                bracketCode.matcher(value);

        if (matcher.find()) {

            return matcher.group(1);
        }

        /*
         * 2. Numeric prefix.
         */
        Matcher numericCode =
                CODE_PREFIX.matcher(value);

        if (numericCode.matches()) {

            return numericCode.group(1);
        }

        /*
         * 3. Generic alphanumeric code.
         */
        Pattern alphaNumericCode =
                Pattern.compile(
                        "\\b[A-Z]{2,6}\\d{3,12}[A-Z0-9]*\\b",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher genericMatcher =
                alphaNumericCode.matcher(value);

        if (genericMatcher.find()) {

            return genericMatcher.group();
        }

        return "";
    }

    /*
     * ============================================================
     * PRODUCT CODE FROM URL
     * ============================================================
     */

    private String extractCodeFromUrl(
            String url) {

        if (url == null
                || url.isBlank()) {
            return "";
        }

        String productName =
                extractProductNameFromUrl(
                        url
                );

        if (!productName.isBlank()) {

            String code =
                    extractCode(
                            productName
                    );

            if (!code.isBlank()) {
                return code;
            }
        }

        Pattern pattern =
                Pattern.compile(
                        "[?&]id=([0-9]{3,6})(?:-|&|$)"
                );

        Matcher matcher =
                pattern.matcher(url);

        if (matcher.find()) {

            return matcher.group(1);
        }

        try {

            String decoded =
                    URLDecoder.decode(
                            url,
                            StandardCharsets.UTF_8
                    );

            Matcher numeric =
                    Pattern.compile(
                            "[?&]id=(\\d{3,6})"
                    ).matcher(decoded);

            if (numeric.find()) {

                return numeric.group(1);
            }

        } catch (Exception ignored) {
        }

        return "";
    }

    /*
     * ============================================================
     * HIDDEN VALUES
     * ============================================================
     */

    private String extractHiddenValue(
            Document document,
            String name) {

        Element element =
                document.selectFirst(
                        "input[name='"
                                + name
                                + "']"
                );

        if (element == null) {

            element =
                    document.selectFirst(
                            "input#"
                                    + name
                    );
        }

        if (element == null) {
            return "";
        }

        return clean(
                element.attr("value")
        );
    }

    /*
     * ============================================================
     * AJAX c_id
     * ============================================================
     */

    private String extractCollectionIdForAjax(
            Document document) {

        String value =
                extractHiddenValue(
                        document,
                        "c_id"
                );

        if (!value.isBlank()) {
            return value;
        }

        for (Element link :
                document.select(
                        "a[href*='cid=']"
                )) {

            String href =
                    link.attr("href");

            Matcher matcher =
                    Pattern.compile(
                            "[?&]cid=(\\d+)"
                    ).matcher(href);

            if (matcher.find()) {

                return matcher.group(1);
            }
        }

        return "";
    }

    /*
     * ============================================================
     * AJAX s_id
     * ============================================================
     */

    private String extractSizeIdForAjax(
            Document document) {

        String value =
                extractHiddenValue(
                        document,
                        "s_id"
                );

        if (!value.isBlank()) {
            return value;
        }

        for (Element link :
                document.select(
                        "a[href*='product-detail.php']"
                )) {

            String href =
                    link.attr("href");

            Matcher matcher =
                    Pattern.compile(
                            "[?&]size=(\\d+)"
                    ).matcher(href);

            if (matcher.find()) {

                return matcher.group(1);
            }
        }

        for (Element link :
                document.select(
                        "a[href*='product-list.php']"
                )) {

            String href =
                    link.attr("href");

            Matcher matcher =
                    Pattern.compile(
                            "[?&]size=(\\d+)"
                    ).matcher(href);

            if (matcher.find()) {

                return matcher.group(1);
            }
        }

        return "";
    }

    /*
     * ============================================================
     * LOAD MORE PRODUCT ID
     * ============================================================
     */

    private String extractLoadMoreProductId(
            Document document) {

        Element showMore =
                document.selectFirst(
                        "span.show_more[id]"
                );

        if (showMore != null) {

            String id =
                    clean(showMore.id());

            if (!id.isBlank()) {
                return id;
            }
        }

        Element generic =
                document.selectFirst(
                        ".show_more[id]"
                );

        if (generic != null) {

            String id =
                    clean(generic.id());

            if (!id.isBlank()) {
                return id;
            }
        }

        for (Element button :
                document.select(
                        "button[id]"
                )) {

            if (button.text()
                    .toLowerCase(
                            Locale.ROOT
                    )
                    .contains(
                            "load more"
                    )) {

                String id =
                        clean(button.id());

                if (!id.isBlank()
                        && !id.equalsIgnoreCase(
                        "submit"
                )) {

                    return id;
                }
            }
        }

        return "";
    }

    /*
     * ============================================================
     * COLLECTION URL
     * ============================================================
     */

    private String buildCollectionUrl(
            String collectionId) {

        return buildBaseUrl()
                + "product-collection.php?collection="
                + URLEncoder.encode(
                        collectionId,
                        StandardCharsets.UTF_8
                );
    }

    /*
     * ============================================================
     * QUERY PARAMETER
     * ============================================================
     */

    private String extractQueryParameter(
            String url,
            String parameter) {

        if (url == null
                || url.isBlank()) {
            return "";
        }

        try {

            URI uri =
                    URI.create(url);

            String query =
                    uri.getQuery();

            if (query == null
                    || query.isBlank()) {
                return "";
            }

            for (String part :
                    query.split("&")) {

                String[] pair =
                        part.split(
                                "=",
                                2
                        );

                if (pair.length == 2
                        && pair[0].equalsIgnoreCase(
                        parameter
                )) {

                    return URLDecoder.decode(
                            pair[1],
                            StandardCharsets.UTF_8
                    );
                }
            }

        } catch (Exception ignored) {
        }

        return "";
    }

    /*
     * ============================================================
     * BASE URL
     * ============================================================
     */

    private String buildBaseUrl() {

        String value =
                baseUrl == null
                        ? ""
                        : baseUrl.trim();

        if (value.isBlank()) {
            return "";
        }

        if (!value.endsWith("/")) {
            value += "/";
        }

        return value;
    }

    /*
     * ============================================================
     * CONNECT
     * ============================================================
     */

    private Connection connect(
            String url) {

        return Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(30000)
                .followRedirects(true)
                .ignoreHttpErrors(false);
    }

    /*
     * ============================================================
     * ABSOLUTE URL
     * ============================================================
     */

    private String absolute(
            String href) {

        if (href == null
                || href.isBlank()) {
            return "";
        }

        href =
                href.trim();

        try {

            if (href.startsWith(
                    "http://"
            )
                    || href.startsWith(
                    "https://"
            )) {

                return href;
            }

            return URI.create(
                    buildBaseUrl()
            ).resolve(
                    href
            ).toString();

        } catch (Exception e) {

            System.err.println(
                    "Unable to build absolute URL: "
                            + href
            );

            return "";
        }
    }

    /*
     * ============================================================
     * NORMALIZE
     * ============================================================
     */

    private String normalize(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .toLowerCase(
                        Locale.ROOT
                )
                .replaceAll(
                        "[^a-z0-9]+",
                        " "
                )
                .trim();
    }

    /*
     * ============================================================
     * SLUG
     * ============================================================
     */

    private String slug(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .toLowerCase(
                        Locale.ROOT
                )
                .replaceAll(
                        "[^a-z0-9]+",
                        "-"
                )
                .replaceAll(
                        "^-|-$",
                        ""
                );
    }

    /*
     * ============================================================
     * CLEAN
     * ============================================================
     */

    private String clean(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }

    /*
     * ============================================================
     * IMPORT KEY
     * ============================================================
     */

    private String buildImportKey(
            String detailUrl,
            String code,
            String name,
            String size) {

        String urlKey =
                normalizeProductUrl(
                        detailUrl
                );

        if (!urlKey.isBlank()) {
            return urlKey;
        }

        if (code != null
                && !code.isBlank()) {

            return code
                    + "|"
                    + normalize(size);
        }

        return slug(name)
                + "|"
                + normalize(size);
    }

    /*
     * ============================================================
     * NORMALIZE PRODUCT URL
     * ============================================================
     */

    private String normalizeProductUrl(
            String url) {

        if (url == null
                || url.isBlank()) {
            return "";
        }

        try {

            String value =
                    url;

            /*
             * Remove size parameter from duplicate key.
             */
            value =
                    value.replaceAll(
                            "([?&])size=[^&]*",
                            "$1"
                    );

            value =
                    value.replace(
                            "?&",
                            "?"
                    );

            value =
                    value.replaceAll(
                            "[?&]$",
                            ""
                    );

            return value;

        } catch (Exception e) {

            return url;
        }
    }

    /*
     * ============================================================
     * DELAY
     * ============================================================
     */

    private void sleep() {

        if (delayMs <= 0) {
            return;
        }

        try {

            Thread.sleep(
                    delayMs
            );

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();
        }
    }
}
