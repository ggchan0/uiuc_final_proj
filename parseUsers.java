import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class parseUsers {

  static final int NUM_USERS = 10305609;
  static final int NUM_USERS_SELECTED = 25;
  static final String USERS_FILE_NAME = "/Users/garrett.chan/Downloads/Users.xml";
  static final String POSTS_FILE_NAME = "/Users/garrett.chan/Downloads/Posts.xml";
  static final String COMMENTS_FILE_NAME = "/Users/garrett.chan/Downloads/Comments.xml";

  static int parseAttrInt(String s) {
    if (s == null) {
      return 0;
    }

    return Integer.parseInt(s);
  }

  static long parseAttrLong(String s) {
    if (s == null) {
      return 0;
    }

    return Long.parseLong(s);
  }

  static String parseAttrString(String s) {
    if (s == null) {
      return "";
    }

    return s.replace("\t", "").replace("\n", "");
  }

  private static Map<String, UserInfo> getUsers() {
    File fXmlFile;
    SAXParserFactory factory;
    SAXParser saxParser;
    TagHandler tagHandler = null;

    try {
      fXmlFile = new File(USERS_FILE_NAME);
      factory = SAXParserFactory.newInstance();
      saxParser = factory.newSAXParser();
      tagHandler = new TagHandler();
      saxParser.parse(fXmlFile, tagHandler);
    } catch (SAXException e) {
      // do nothing
    } catch (Exception e) {
      e.printStackTrace();
    }

    return tagHandler.getMap();
  }

  private static void populateMap(Map<String, UserInfo> map, Handler handler, String file_name) {
    File fXmlFile;
    SAXParserFactory factory;
    SAXParser saxParser;
    handler.setMap(map);

    try {
      fXmlFile = new File(file_name);
      factory = SAXParserFactory.newInstance();
      saxParser = factory.newSAXParser();
      saxParser.parse(fXmlFile, handler);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void createCsv(Map<String, UserInfo> map) throws Exception {
    // String header =
    //
    // "id\treputation\taboutMe\tupvotes\tdownvotes\tviews\tcomments\tposts\ttags\tcommentScore\tpostScore\tpostViewCount\tanswerCount\tcommentCount\tfavoriteCount\n";

    for (String id : map.keySet()) {
      // FileWriter fileWriter = new FileWriter("output-" + id + ".tsv");
      FileWriter fileWriter = new FileWriter("output-" + id + ".txt");
      PrintWriter printWriter = new PrintWriter(fileWriter);
      // printWriter.write(header);
      UserInfo user = map.get(id);
      StringBuilder builder = new StringBuilder();
      /*
      builder.append(id + "\t");
      builder.append(user.reputation + "\t");
      builder.append(user.aboutMe + "\t");
      builder.append(user.upvotes + "\t");
      builder.append(user.downvotes + "\t");
      builder.append(user.views + "\t");
      for (String s : user.commentList) {
        builder.append(s + " ");
      }
      builder.append("\t");
      for (String s : user.postList) {
        builder.append(s + " ");
      }
      builder.append("\t");
      for (String s : user.postTags) {
        builder.append(s + " ");
      }
      builder.append("\t");
      builder.append(user.commentScore + "\t");
      builder.append(user.postScore + "\t");
      builder.append(user.postViewCount + "\t");
      builder.append(user.answerCount + "\t");
      builder.append(user.commentCount + "\t");
      builder.append(user.favoriteCount + "\n");
      */
      for (String s : user.commentList) {
        String target = s.replaceAll("(?i)<td[^>]*>", " ").replaceAll("\\s+", " ").trim();
        // target = target.replaceAll("(?i)<td[^>]*>", " ").replaceAll("\\s+", " ").trim();
        target = target.replaceAll("\\<[^>]*>", "");
        // target = Jsoup.parse(target).text();
        builder.append(target + " ");
      }
      for (String s : user.postList) {
        String target = s.replaceAll("(?i)<td[^>]*>", " ").replaceAll("\\s+", " ").trim();
        // target = target.replaceAll("(?i)<td[^>]*>", " ").replaceAll("\\s+", " ").trim();
        target = target.replaceAll("\\<[^>]*>", "");
        builder.append(target + " ");
      }
      for (String s : user.postTags) {
        String target = s.replaceAll("(?i)<td[^>]*>", " ").replaceAll("\\s+", " ").trim();
        // target = target.replaceAll("(?i)<td[^>]*>", " ").replaceAll("\\s+", " ").trim();
        target = target.replaceAll("\\<[^>]*>", "");
        builder.append(target + " ");
      }
      printWriter.write(builder.toString());
      printWriter.close();
    }
  }

  public static void main(String[] argv) {
    Map<String, UserInfo> map = new HashMap<String, UserInfo>();
    populateMap(map, new TagHandler(), USERS_FILE_NAME);
    populateMap(map, new PostHandler(), POSTS_FILE_NAME);
    populateMap(map, new CommentHandler(), COMMENTS_FILE_NAME);

    try {
      createCsv(map);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

class Handler extends DefaultHandler {
  Map<String, UserInfo> map;

  Handler() {
    super();
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    // nothing
  }

  @Override
  public void characters(char ch[], int start, int length) throws SAXException {
    // nothing
  }

  public void setMap(Map<String, UserInfo> map) {
    this.map = map;
  }

  public Map<String, UserInfo> getMap() {
    return this.map;
  }
}

class TagHandler extends Handler {

  int num_users;

  TagHandler() {
    super();
    num_users = 0;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException {
    if (!qName.equalsIgnoreCase("row")) {
      return;
    }
    if (attributes.getValue("Id").equals("-1")) {
      return;
    }

    int id = parseUsers.parseAttrInt(attributes.getValue("Id"));
    long reputation = parseUsers.parseAttrLong(attributes.getValue("Reputation"));
    String aboutMe = parseUsers.parseAttrString(attributes.getValue("AboutMe"));
    int upvotes = parseUsers.parseAttrInt(attributes.getValue("UpVotes"));
    int downvotes = parseUsers.parseAttrInt(attributes.getValue("DownVotes"));
    long views = parseUsers.parseAttrLong(attributes.getValue("Views"));

    System.out.println(attributes.getValue("Id"));
    map.put(
        attributes.getValue("Id"),
        new UserInfo(id, reputation, aboutMe, upvotes, downvotes, views));

    if (++num_users == 25) {
      throw new SAXException();
    }
  }
}

class PostHandler extends Handler {
  PostHandler() {
    super();
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException {
    if (!qName.equalsIgnoreCase("row")) {
      return;
    }

    if (!map.containsKey(attributes.getValue("OwnerUserId"))) {
      return;
    }

    UserInfo user = map.get(attributes.getValue("OwnerUserId"));

    user.postScore += parseUsers.parseAttrInt(attributes.getValue("Score"));
    user.postViewCount += parseUsers.parseAttrInt(attributes.getValue("ViewCount"));
    user.answerCount += parseUsers.parseAttrInt(attributes.getValue("AnswerCount"));
    user.commentCount += parseUsers.parseAttrInt(attributes.getValue("CommentCount"));
    user.favoriteCount += parseUsers.parseAttrInt(attributes.getValue("FavoriteCount"));
    user.postList.add(parseUsers.parseAttrString(attributes.getValue("Body")));

    String[] tags =
        parseUsers
            .parseAttrString(attributes.getValue("Tags"))
            .replace("&lt;", " ")
            .replace("&gt;", " ")
            .split(" ");
    for (String tag : tags) {
      String trimmed = tag.trim();
      if (trimmed.length() > 0) {
        user.postTags.add(trimmed);
      }
    }
  }
}

class CommentHandler extends Handler {
  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException {
    if (!qName.equalsIgnoreCase("row")) {
      return;
    }

    if (!map.containsKey(attributes.getValue("UserId"))) {
      return;
    }

    UserInfo user = map.get(attributes.getValue("UserId"));

    user.commentScore += parseUsers.parseAttrInt(attributes.getValue("Score"));
    user.commentList.add(parseUsers.parseAttrString(attributes.getValue("Text")));
  }
}

class UserInfo {
  int id;
  long reputation;
  String aboutMe;
  int upvotes;
  int downvotes;
  long views;
  List<String> commentList;
  List<String> postList;
  Set<String> postTags;
  long commentScore;
  long postScore;
  long postViewCount;
  long answerCount;
  long commentCount;
  long favoriteCount;

  public UserInfo(int id, long reputation, String aboutMe, int upvotes, int downvotes, long views) {
    this.id = id;
    this.reputation = reputation;
    this.aboutMe = aboutMe;
    this.upvotes = upvotes;
    this.downvotes = downvotes;
    this.views = views;
    commentList = new ArrayList<>();
    postList = new ArrayList<>();
    postTags = new HashSet<>();
    commentScore = 0;
    postScore = 0;
    postViewCount = 0;
    answerCount = 0;
    commentCount = 0;
    favoriteCount = 0;
  }
}
