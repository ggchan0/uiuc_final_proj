import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/** Contains the main method that is run and all the logic to parse the required files into */
public class parseUsers {

  static final int DEFAULT_MAX_NUM_USERS = 25;
  static String USERS_FILE_NAME;
  static String POSTS_FILE_NAME;
  static String COMMENTS_FILE_NAME;

  // A safe way to parse an integer given a string with a null check
  static int parseAttrInt(String s) {
    if (s == null) {
      return 0;
    }

    return Integer.parseInt(s);
  }

  // A safe way to parse a long given a string with a null check
  static long parseAttrLong(String s) {
    if (s == null) {
      return 0;
    }

    return Long.parseLong(s);
  }

  // A safe way to do String validation with a null check
  // Also removes any tab (\t), newline (\n), or HTML tag from the string
  static String parseAttrString(String s) {
    if (s == null) {
      return "";
    }

    return s.replaceAll("(?i)<td[^>]*>", " ")
        .replaceAll("\\s+", " ")
        .replaceAll("\\<[^>]*>", "")
        .replace("\t", "")
        .replace("\n", "");
  }

  // Method to create a map of IDs to the corresponding UserInfo
  // Utilizes polymorphism to reuse code and reduce the amount of work needed to parse
  // three different kinds of files into usable data
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
    } catch (SAXException e) {
      // swallow
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  // Creates the output file from all the processed users
  private static void createOutput(Map<String, UserInfo> map) throws Exception {
    for (String id : map.keySet()) {
      FileWriter fileWriter = new FileWriter("../output-" + id + ".txt");
      PrintWriter printWriter = new PrintWriter(fileWriter);
      UserInfo user = map.get(id);
      StringBuilder builder = new StringBuilder();
      // For each comment, post, or tag, do some string
      for (String s : user.commentList) {
        builder.append(s + " ");
      }
      for (String s : user.postList) {
        builder.append(s + " ");
      }
      for (String s : user.postTags) {
        builder.append(s + " ");
      }

      printWriter.write(builder.toString());
      printWriter.close();
    }
  }

  public static void main(String[] argv) {
    Map<String, UserInfo> map = new HashMap<String, UserInfo>();
    USERS_FILE_NAME = argv[0];
    POSTS_FILE_NAME = argv[1];
    COMMENTS_FILE_NAME = argv[2];
    // Set the number of users to parse through if it is passed in
    if (argv.length > 3) {
      int numUsers = parseUsers.parseAttrInt(argv[3]);
      populateMap(map, new TagHandler(numUsers), USERS_FILE_NAME);
      // Otherwise, use the default value
    } else {
      populateMap(map, new TagHandler(), USERS_FILE_NAME);
    }
    populateMap(map, new PostHandler(), POSTS_FILE_NAME);
    populateMap(map, new CommentHandler(), COMMENTS_FILE_NAME);

    try {
      createOutput(map);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

/**
 * Handler which will be morphed into a TagHandler, PostHandler, or CommentHandler Class creates a
 * map of an ID to the corresponding UserInfo, and allows methods to access it
 */
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

/**
 * Implementation of Handler to parse through the Users.xml file Parses through max_num_users (if
 * given) or DEFAULT_MAX_NUM_USERS which is currently set to 25 users. Class will create a map of
 * the ID to the UserInfo This will throw a SAXException when it processes max_num_users users to
 * stop parsing through the file
 */
class TagHandler extends Handler {

  int num_users;
  int max_num_users;

  TagHandler() {
    super();
    num_users = 0;
    this.max_num_users = parseUsers.DEFAULT_MAX_NUM_USERS;
  }

  TagHandler(int max_num_users) {
    super();
    num_users = 0;
    this.max_num_users = max_num_users;
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

    map.put(
        attributes.getValue("Id"),
        new UserInfo(id, reputation, aboutMe, upvotes, downvotes, views));

    // If we process the desired amount of users, throw an exception to stop processing data
    if (++num_users == max_num_users) {
      throw new SAXException();
    }
  }
}

/**
 * Implementation of Handler to parse through the Posts.xml file. This will go through every post in
 * the xml file and get every post and related metadata for users that are specified within the
 * given map.
 */
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

/**
 * Implementation of Handler which will parse through the Comments.xml file. Adds the corresponding
 * comments to every user in the map.
 */
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

/**
 * Plain Old Java Object to hold data about a given user. At the current moment, we only use the
 * comments, posts, and tags of the user. If this project were to continue, additional metadata
 * about the user can be utilized to create a more powerful ranking system.
 */
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
