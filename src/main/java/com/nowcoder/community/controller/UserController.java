package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.*;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeaderImage(MultipartFile headerImage, Model model) {
        if (null == headerImage) {
            model.addAttribute("error", "???????????????????????????");
            return "/site/setting";
        }
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "?????????????????????");
            return "/site/setting";
        }
        // ?????????????????????
        fileName = CommunityUtil.generateUUID() + suffix;
        // ????????????????????????
        File dest = new File(uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException exception) {
            logger.error("??????????????????: " + exception.getMessage());
            throw new RuntimeException("?????????????????????????????????????????????", exception);
        }
        // ???????????????????????????Web???????????????
        User user = hostHolder.getUser();
        // http://localhost:8080/community/user/header/fileName
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";
    }

    /**
     * BUG???????????????????????????????????????????????????
     * DEBUG?????????????????????????????????domain????????????????????????????????????
     */
    @GetMapping("/header/{fileName}")
    public void getUserHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // ????????????????????????
        fileName = uploadPath + "/" + fileName;
        // ????????????
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // ????????????
        response.setContentType("image/" + suffix);
        try (
                OutputStream os = response.getOutputStream();
                FileInputStream fileInputStream = new FileInputStream(fileName);
        ) {
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = fileInputStream.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            logger.error("??????????????????: " + e.getMessage());
        }
    }

    // ????????????
    @LoginRequired
    @PostMapping("/updatePassword")
    public String updatePassword(String oldPassword, String newPassword, Model model) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);
        if (map == null || map.isEmpty()) {
            return "redirect:/logout";
        } else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "/site/setting";
        }
    }

    // ????????????
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("?????????????????????");
        }

        // ??????
        model.addAttribute("user", user);
        // ????????????
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // ????????????
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

        // ????????????
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        // ???????????????
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/profile";
    }

    // ????????????
    @RequestMapping(path = "/mypost/{userId}", method = RequestMethod.GET)
    public String getMyPost(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("?????????????????????");
        }
        model.addAttribute("user", user);

        // ????????????
        page.setPath("/user/mypost/" + userId);
        page.setRows(discussPostService.findDiscussPostRows(userId));

        // ????????????
        List<DiscussPost> discussList = discussPostService
                .findDiscussPosts(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussVOList = new ArrayList<>();
        if (discussList != null) {
            for (DiscussPost post : discussList) {
                Map<String, Object> map = new HashMap<>();
                map.put("discussPost", post);
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                discussVOList.add(map);
            }
        }
        model.addAttribute("discussPosts", discussVOList);

        return "/site/my-post";
    }

    // ????????????
    @RequestMapping(path = "/myreply/{userId}", method = RequestMethod.GET)
    public String getMyReply(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("?????????????????????");
        }
        model.addAttribute("user", user);

        // ????????????
        page.setPath("/user/myreply/" + userId);
        page.setRows(commentService.findUserCount(userId));

        // ????????????
        List<Comment> commentList = commentService.findUserComments(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentVOList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);
                DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
                map.put("discussPost", post);
                commentVOList.add(map);
            }
        }
        model.addAttribute("comments", commentVOList);

        return "/site/my-reply";
    }
}
