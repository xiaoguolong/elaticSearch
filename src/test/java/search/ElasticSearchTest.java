package search;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.BeanUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.TotalHits;
import org.apache.poi.ss.formula.functions.T;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheRequest;
import org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.search.MatchQuery;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.MinAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.SumAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class ElasticSearchTest {

    private static TransportClient client;

    //private static final Logger logger = LogManager.getLogger(Logger.class.getName());
    private static Logger logger = null;
    private static RestHighLevelClient highLevelClient;

    private static final String STORE="store";
    private static final String KEYWORD="keyword";
    private static final String INDEX="teacher";


    static {
        initHighLevelClient();
    }

    /**
     * 初始化客户端
     *
     * @throws Exception
     */
    private static void init() throws Exception {

        //Configuration config = new XMLConfiguration("logback.xml");
        client = new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"), 9300));
    }

    /**
     * 关闭客户端
     *
     * @throws Exception
     */
    private static void closeClient() throws Exception {
        if (null == client) {
            init();
        }
        client.close();
    }

    private static void initLogger() throws Exception {
        System.setProperty("log4j.configurationFile", "log4j2.xml");
        logger = LogManager.getLogger();
    }

    private static void createIndex() throws Exception {
        /**
         * json形式
         */
        String jsonStr = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2019-06-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";
        /*IndexResponse response = client.prepareIndex("twitter", "_doc", "1")
                .setSource(jsonStr, XContentType.JSON)
                .get();*/
        /**
         * map形式
         */
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("user", "kimchy");
        json.put("postDate", new Date());
        json.put("message", "trying out Elasticsearch");
        /*IndexResponse response = client.prepareIndex("twitter", "_doc", "1")
                .setSource(json)
                .get();*/
        /**
         * serialize bean 实体方式
         */
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();// create once, reuse
        TeacherVo teacherVo = new TeacherVo();
        teacherVo.setId(1L);
        teacherVo.setLecturerName("测试老师");
        teacherVo.setPic("https://images2015.cnblogs.com/blog/844237/201610/844237-20161026152754593-519886421.jpg");
        /*byte[] jsonSerializable = mapper.writeValueAsBytes(new TeacherVo());
        IndexResponse response = client.prepareIndex("twitter", "_doc", "1")
                .setSource(jsonSerializable,XContentType.)
                .get();*/
        /**
         * Elasticsearch helper形式
         */
        XContentBuilder builder = jsonBuilder()
                .startObject()
                .field("user", "kimchy")
                .field("postDate", new Date())
                .field("message", "trying out Elasticsearch")
                .endObject();
        /**
         *
         */
        if (null == client) {
            init();
        }
        IndexResponse response = client.prepareIndex("twitter", "_doc", "1")
                .setSource(builder)
                .get();

        // Index name
        String _index = response.getIndex();
        // Type name
        String _type = response.getType();
        // Document ID (generated or not)
        String _id = response.getId();
        // Version (if it's the first time you index this document, you will get: 1)
        long _version = response.getVersion();
        // status has stored current instance statement.
        RestStatus status = response.status();
    }

    private  static void getIndex()throws Exception{
        if (null == client) {
            init();
        }
        GetResponse response = client.prepareGet("twitter", "_doc", "1").get();

    }

    public static TransportClient getClient() {
        return client;
    }

    /**
     * 利用java高可用客户端 highLevelClient api
     * @param
     * @throws Exception
     */
    public  static void useHighLevelClient() throws Exception{
        /**
         * 创建索引 参照网上与行家项目 利用反射根据类字段注解与类型创建
         */
        //索引名称
        String indexName="teacher".toLowerCase();//必须小写
        //字节类型
        Class clazz=TeacherVo.class;
        //获取字节字段
        Field[] fields = clazz.getFields();
        Settings settings=Settings.builder()
                /*默认3个主分片*/
                .put("number_of_shards", 3)
                /*默认0个复制分片==>单机版配置;集群副本数量最多不超过2==>副本越多,性能越低*/
                .put("number_of_replicas", 0).build();
        try{
            //创建索引
            if(StringUtils.isNotBlank(indexName)){
                CreateIndexRequest indexRequest = new CreateIndexRequest(indexName);
                indexRequest.settings(settings);
                if(null ==highLevelClient){
                    initHighLevelClient();
                }
                highLevelClient.indices().create(indexRequest, RequestOptions.DEFAULT);
            }
        }catch (Exception e){
            e.printStackTrace();
            try {
                highLevelClient
                        .indices()
                        .delete(new DeleteIndexRequest(indexName),RequestOptions.DEFAULT);
                /*重建索引*/
                CreateIndexRequest indexRequest = new CreateIndexRequest(indexName);
                indexRequest.settings(settings);
                highLevelClient.indices().create(indexRequest,RequestOptions.DEFAULT);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        //映射字段
        XContentBuilder builder = null;
        try {
            builder = XContentFactory.jsonBuilder();
            builder.startObject()
                    .startObject(indexName)
                    .startObject("_all")
                    /*关闭_all字段*/
                    .field("enabled", false)
                    .endObject()
                    .startObject("_source")
                    .field("enabled", true)
                    .endObject()
                    /*properties 为包含文档所有的属性字段*/
                    .startObject("properties");
            /*映射字段转换*/
            combineClassInfo(builder, fields);
            builder.endObject();
            builder.endObject();
            builder.endObject();
        } catch (Exception e) {
            System.out.println("文档结构创建失败");
        }

    }

    private static void combineClassInfo(XContentBuilder builder, Field[] fields) throws IOException{
        for (Field field : fields) {
            switchType(builder, field);
        }
    }

    private static void switchType(XContentBuilder builder, Field field) throws IOException {
        String name = field.getName();
        builder.startObject(name);
        String typeName = field.getGenericType().getTypeName();
        try {
            switch (typeName) {
                case "int":
                case "long":
                case "java.lang.Integer":
                case "java.lang.Long":
                    builder.field(STORE, true);
                    builder.field("type", "long").endObject();
                    break;
                case "java.lang.String":
                    builder.field(STORE, true);
                    if (field.getAnnotation(ElaticSearchAnlyzer.class)!=null){
                        //被analyzer修饰
                        ElaticSearchAnlyzer annotation = field.getAnnotation(ElaticSearchAnlyzer.class);
                        String analyzer = annotation.analyzer();
                        String type = annotation.type();
                        if (type.equals(KEYWORD)){
                            builder.field("type", KEYWORD).endObject();
                        }else {
                            if (analyzer.equals("ik_smart")){
                                builder.field("type", "text");
                                builder.field("analyzer","ik_smart").endObject();
                            }
                            if (analyzer.equals("ik_max_word")){
                                builder.field("type", "text");
                                builder.field("analyzer","ik_max_word").endObject();
                            }
                        }
                    }else {
                        builder.field("type", KEYWORD).endObject();
                    }
                    break;
                case "java.sql.Date":
                case "java.util.Date":
                    builder.field(STORE, true);
                    builder.field("type", "date")
                            .field("format", "yyyy-MM-dd HH:mm:ss || yyyy-MM-dd || epoch_millis")
                            .endObject();
                    break;
                case "boolean":
                case "java.lang.Boolean":
                    builder.field(STORE, true);
                    builder.field("type", "boolean").endObject();
                    break;
                case "java.lang.Double":
                    builder.field(STORE, true);
                    builder.field("type", "double").endObject();
                case "java.math.BigDecimal":
                    builder.field(STORE, true);
                    builder.field("type", "BigDecimal").endObject();
                    break;
                default:
                    builder.field(STORE, true);
                    builder.field("type", "text").endObject();
            }
        } catch (Exception e) {
            System.out.println("[类型异常]");
        }
    }

    /**
     * 初始化highLevelClient
     * @param
     * @throws Exception
     */
    public  static void initHighLevelClient(){
        //设置highLevelClient http 连接属性
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        //如果设置身份验证,则需要输入设置的用户密码
        //credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(null, null));

        RestClientBuilder builder = RestClient.builder(new HttpHost[] { new HttpHost("127.0.0.1", 9201,"http") })
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback()
        {
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder)
            {
                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        });
        highLevelClient = new RestHighLevelClient(builder);

    }

    /**
     * 插入(更新)数据
     *
     * @param index        索引名称
     * @param type         类型名称
     * @param key          主键
     * @param entityParams 实体
     */
   public static IndexResponse insertDocuments(String index, String type, String key, Map<String, Object> entityParams){
       IndexRequest indexRequest = getIndexRequest(index, type, key, entityParams);
       IndexResponse indexResponse = null;
       try {
           indexResponse = highLevelClient.index(indexRequest,RequestOptions.DEFAULT);
       } catch (Exception e) {
           e.printStackTrace();
       }
       if(null!=indexResponse){
           if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
               System.out.println("INDEX CREATE SUCCESS");
           } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
               System.out.println("INDEX UPDATE SUCCESS");
           }
       }
        return indexResponse;
   }

    /**
     * 批量插入(更新)数据
     *
     * @param index            索引名称
     * @param type             类型名称
     * @param key              主键字段
     * @param entityParamsList 实体属性集合
     * @return 批量插入结果
     */
    public static BulkResponse documentsInsert(String index, String type, String key, List<Map<String, Object>> entityParamsList){
        BulkRequest bulkRequest = new BulkRequest();
        for (Map<String, Object> entityParams : entityParamsList) {
            IndexRequest indexRequest = getIndexRequest(index, type, key, entityParams);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulkResponse = null;
        try {
            bulkResponse = highLevelClient.bulk(bulkRequest,RequestOptions.DEFAULT);
        } catch (Exception e) {
            System.out.println("出现异常:{}");
            e.printStackTrace();
        }
        return bulkResponse;
    }

    /**
     * 数据转换(保存操作-Map转IndexRequest)
     *
     * @param index
     * @param type
     * @param key
     * @param entityParams
     * @return
     */
    private static IndexRequest getIndexRequest(String index, String type, String key, Map<String, Object> entityParams) {
        Object id = entityParams.get(key);
        IndexRequest indexRequest = null;
        if (null == id) {
            indexRequest = new IndexRequest(index, type);
        } else {
            indexRequest = new IndexRequest(index, type, id.toString());
        }
        String source = JSON.toJSONString(entityParams);
        indexRequest.source(source, XContentType.JSON);
        return indexRequest;

    }

    /**
     * 删除索引
     * @param index
     * @return
     */
    public static Boolean deleteIndex(String index) {
        /**
         *
         * 如果找不到索引，则会抛出ElasticsearchException：
         */
        try {
            /**
             * 指定要删除的索引名称
             */
            DeleteIndexRequest request = new DeleteIndexRequest(index);
            highLevelClient.indices().delete(request,RequestOptions.DEFAULT);
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    /**
     * 根据ID,删除文档
     *
     * @param index      索引名称
     * @param type       类型名称
     * @param documentId 文档id
     * @return 删除结果集
     */
    public static DeleteResponse deleteDocument(String index, String type, String documentId) {
        DeleteRequest deleteRequest = new DeleteRequest(index, type, documentId);
        DeleteResponse deleteResponse = null;
        try {
            deleteResponse = highLevelClient.delete(deleteRequest,RequestOptions.DEFAULT);
        } catch (IOException e) {
            System.out.println("出现异常");
        }
        if (null!=deleteResponse && deleteResponse.getResult() == DocWriteResponse.Result.DELETED) {
            System.out.println("删除文档成功");
        }
        return deleteResponse;
    }

    /**
     * 批量删除文档
     *
     * @param index
     * @param type
     * @param idList
     * @return
     */
    public static BulkResponse deleteDocuments(String index, String type, List<String> idList){
        BulkRequest bulkRequest = new BulkRequest();
        for (String key : idList) {
            DeleteRequest deleteRequest = new DeleteRequest(index, type, key);
            bulkRequest.add(deleteRequest);
        }
        BulkResponse bulkResponse = null;
        try {
            bulkResponse = highLevelClient.bulk(bulkRequest,RequestOptions.DEFAULT);
        } catch (Exception e) {
            System.out.println("出现异常:{}");
            e.printStackTrace();
        }
        return bulkResponse;
    }

    /**
     * 根据id获取文档内容,自动转化返回实体
     *
     * @param index
     * @param type
     * @param documentId
     * @return
     */
    public static String getDocument(String index, String type, String documentId) {
        GetRequest getRequest = new GetRequest(index, type, documentId);
        GetResponse getResponse = null;
        try {
            getResponse = highLevelClient.get(getRequest,RequestOptions.DEFAULT);
        } catch (IOException e) {
            System.out.println("获取文档异常");
        }
        if (null!=getResponse && getResponse.isExists()) {
            //return JSONObject.parseObject(getResponse.getSourceAsString(), clazz);
            System.out.println("getResponse.getSourceAsString()>>>>>>>>>"+getResponse.getSourceAsString());
            return getResponse.getSourceAsString();
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        //init();
        //closeClient();
        //getIndex();
        //useHighLevelClient();
        //deleteIndex(INDEX);
        /*TeacherVo teacherVo = new TeacherVo();
        teacherVo.setId(11L);
        teacherVo.setPic("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1566477845351&di=10c8a6692a65c243ddaffe7e9e8f21f1&imgtype=0&src=http%3A%2F%2Fimg1.ph.126.net%2FgcFm3FuT3LSIJUG94a83GA%3D%3D%2F6619114974794269915.jpg");
        teacherVo.setLecturerName("宋老师");
        teacherVo.setLecturerIntro("授课幽默");
        teacherVo.setPrice(getPriceFormat(new BigDecimal("33.50")));
        teacherVo.setAge(28);
        teacherVo.setBirthDate(strToDate("1991-04-28"));
        Map map = JSONObject.parseObject(JSON.toJSONString(teacherVo), Map.class);
        insertDocuments(INDEX,INDEX,"id",map);*/
        /**
         * 批量插入数据
         */
        /*TeacherVo teacherVo = new TeacherVo();
        teacherVo.setId(3L);
        teacherVo.setPic("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1566477845351&di=10c8a6692a65c243ddaffe7e9e8f21f1&imgtype=0&src=http%3A%2F%2Fimg1.ph.126.net%2FgcFm3FuT3LSIJUG94a83GA%3D%3D%2F6619114974794269915.jpg");
        teacherVo.setLecturerName("宋老师");
        teacherVo.setLecturerIntro("认真");
        TeacherVo teacherVo1 = new TeacherVo();
        teacherVo1.setId(4L);
        teacherVo1.setPic("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1566477845351&di=10c8a6692a65c243ddaffe7e9e8f21f1&imgtype=0&src=http%3A%2F%2Fimg1.ph.126.net%2FgcFm3FuT3LSIJUG94a83GA%3D%3D%2F6619114974794269915.jpg");
        teacherVo1.setLecturerName("甘老师");
        teacherVo1.setLecturerIntro("知识渊博");
        List<Map<String,Object>> mapList = new ArrayList<>();
        mapList.add(Object2Map(teacherVo));
        mapList.add(Object2Map(teacherVo1));
        documentsInsert(INDEX,INDEX,"id",mapList);*/
        /**
         * 批量删除文档
         */
        /*List<String> idList = new ArrayList<>();
        idList.add("4");
        deleteDocuments(INDEX,INDEX,idList);*/
        /**
         * 删除文档
         */
        //deleteDocument(INDEX,INDEX,"1");
        /**
         * 根据id获取文档
         */
        //getDocument(INDEX,INDEX,"1");

        //构建搜索参数,并搜索
        search();


    }

    /**
     * 搜索方法
     */
    private static void search() throws  Exception{
        /**
         * 搜索
         */
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        /**
         * boolQueryBuilder: must mustNot should
         */
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        /**
         * queryBuilder:
         *      matchAllQuery     查询指定索引下的所有文档

         *      fuzzyQuery        模糊匹配 只能用在 keyword text 类型字段 其它的会报错

         *      matchQuery        分词搜索 参数会被分词分解去查询  非text/keyword 字段效果相当于termQuery

         *      matchPhraseQuery  词语搜索 根据词语参数去查询   非text/keyword 字段效果相当于termQuery

         *      queryStringQuery  根据值去每个字段进行模糊查询 +代表必须含有  -代表不能含有 >>>>>>>>少用 低效率

         *      termQuery         完全匹配，即不进行分词器分析，字段中必须包含整个搜索的词汇

         *      termsQuery        在term基础上增加一个字段对应多个值  相当sql中的in >>>>>>>>string类型的话 参数只能是一个中文,否则匹配不到正确的文档

         *      wildcardQuery     通配符匹配  限于text/keyword 字段
         *
         *      prefixQuery       前缀查询 根据参数模糊查询   限于text/keyword 字段
         *
         *      idsQuery          文档id 可以多个查询
         *
         *      typeQuery         一个索引多个type时可以使用
         *
         *      regexpQuery       正则查询 作用于string类型字段
         *
         *      ....
         */
        //TermQueryBuilder termQueryBuilder = new TermQueryBuilder("age",8);
        //matchAllQuery
        //MatchAllQueryBuilder allQueryBuilder = new MatchAllQueryBuilder();
        List<String> NameList = new ArrayList<>();
        NameList.add("仓");
        NameList.add("宋");
        /**
         * sql in
         */
        TermsQueryBuilder termsQueryBuilder = new TermsQueryBuilder("lecturerName", NameList);
        /**
         * 分词搜索   多个字段
         */
        //MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("age", 25);
        //MatchQueryBuilder matchQueryBuilder1 = new MatchQueryBuilder("lecturerName", "宋老湿");
        //MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("lecturerIntro", "讲幽");
        //词语搜索
        //MatchPhraseQueryBuilder matchPhraseQueryBuilder = new MatchPhraseQueryBuilder("id", "1");
        //分词搜索
        //MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("lecturerName", "宋老湿");
        //模糊匹配
        //matchQueryBuilder.fuzziness(Fuzziness.AUTO);/*模糊查询只能用在 keyword text 类型字段 其它的会报错*/
        //根据值去每个字段进行模糊查询 +代表必须含有  -代表不能含有
        //QueryStringQueryBuilder queryStringQueryBuilder = new QueryStringQueryBuilder("+2 -3");
        //通配符
        //WildcardQueryBuilder wildcardQueryBuilder = new WildcardQueryBuilder("lecturerName", "zhao*");
        //前缀
        //PrefixQueryBuilder prefixQueryBuilder = new PrefixQueryBuilder("id", "仓*");
        //模糊查询
        //FuzzyQueryBuilder fuzzyQueryBuilder = new FuzzyQueryBuilder("lecturerName", "仓");
        //ids查询
        /*IdsQueryBuilder idsQueryBuilder = new IdsQueryBuilder();
        idsQueryBuilder.addIds("1","2");
        idsQueryBuilder.addIds("2");*/
        //正则查询
        RegexpQueryBuilder regexpQueryBuilder = new RegexpQueryBuilder("lecturerIntro", "[^湿]");



        /**
         * 多个字段之间 must mustNot should
         */
        //boolQueryBuilder.must(matchQueryBuilder);
        //boolQueryBuilder.must(matchPhraseQueryBuilder);
        //boolQueryBuilder.must(queryStringQueryBuilder);
        //boolQueryBuilder.must(queryStringQueryBuilder);

        boolQueryBuilder.must(regexpQueryBuilder);
        
        searchSourceBuilder.query(boolQueryBuilder);
        /**
         * 排序
         */
        searchSourceBuilder.sort("birthDate",SortOrder.DESC);
        searchSourceBuilder.sort("id",SortOrder.DESC);
        /**
         * 聚合查询 对所有的文档进行操作
         */
        //最小
        //MinAggregationBuilder minAgeBuilder = AggregationBuilders.min("minAge").field("age");
        //最大
        //MaxAggregationBuilder maxAggregationBuilder = AggregationBuilders.max("maxAge").field("age");
        //平均
        AvgAggregationBuilder avgAggregationBuilder = AggregationBuilders.avg("avgAge").field("age");
        //求和
        SumAggregationBuilder sumAggregationBuilder = AggregationBuilders.sum("sumAge").field("age");
        //范围 >>>>>>>> 非text,keyword类型字段
        //RangeAggregationBuilder rangeAggregationBuilder = AggregationBuilders.range("priceRange").field("price").addRange(30,30.1);
        //RangeAggregationBuilder rangeAggregationBuilder = AggregationBuilders.range("priceRange").field("price").addUnboundedFrom(30,30.1);
        RangeAggregationBuilder rangeAggregationBuilder = AggregationBuilders.range("priceRange").field("price").addUnboundedTo(31);

        //.......具体需求    看AggregationBuilders方法
        searchSourceBuilder.aggregation(rangeAggregationBuilder);
        /**
         * 分页
         */
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(2);


        SearchRequest searchRequest = new SearchRequest(INDEX);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        if(null != searchResponse) {
            SearchHits hits = searchResponse.getHits();
            SearchHit[] hitsHits = hits.getHits();
            /**
             * 总条数
             */
            TotalHits totalHits = hits.getTotalHits();
            String totalStr = JSON.toJSONString(totalHits);
            Map totalCountMap = JSON.parseObject(totalStr, Map.class);
            Object totalCount = totalCountMap.get("value");
            System.out.println("查询总数>>>>>>>>"+totalCount+"条");
            /**
             * 查询结果
             */
            if (null != hitsHits && hitsHits.length > 0) {
                List<TeacherVo> teacherVos = Arrays.stream(hitsHits).filter(Objects::nonNull).map(SearchHit::getSourceAsString).map(e -> {
                    return JSON.parseObject(e, TeacherVo.class);
                }).collect(Collectors.toList());
                System.out.println(teacherVos.toString());
            }
            /**
             * 查询结果的集合计算结果
             */
            Aggregations aggregations = searchResponse.getAggregations();
            if(null != aggregations){
                /**
                 * result 结果的key对应 max,min,avg,range...操作对应的别名......max(key)
                 */
                //String result = JSON.toJSONString(aggregations.getAsMap().get("minAge"));
                //String result = JSON.toJSONString(aggregations.getAsMap().get("maxAge"));
                //String result = JSON.toJSONString(aggregations.getAsMap().get("avgAge"));
                String result = JSON.toJSONString(aggregations.getAsMap().get("priceRange"));
                Map map = JSON.parseObject(result, Map.class);
                String type = map.get("type").toString();
                Object value=null;
                switch (type){
                    case "min":
                    case "max":
                    case "avg":
                    case "sum":
                        value = map.get("value");
                        break;
                    case "range":
                        /**
                         * 范围文档数
                         */
                        JSONArray buckets = JSON.parseArray(JSON.toJSONString(map.get("buckets")));
                        Object o = buckets.get(0);
                        Map rangeResult = JSON.parseObject(o.toString(), Map.class);
                        Object docCount = rangeResult.get("docCount");
                        value=docCount;
                    default:
                        break;
                }
                System.out.println(value);
            }

        }
        System.out.println(searchResponse);
    }

    public static Date strToDate(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }

    public static Map<String,Object> Object2Map(Object object){
        return Optional.ofNullable(object).map(e->{
            return JSONObject.parseObject(JSON.toJSONString(object), Map.class);

        }).orElse(null);
    }


    /**
     * BigDecimal 保留两位小数
     */
    public static BigDecimal getPriceFormat(BigDecimal price) {
        if (price == null) {
            price = new BigDecimal("0.00");
        }
        String decimalFormat = new DecimalFormat("0.00").format(price);
        return new BigDecimal(decimalFormat);
    }

}




