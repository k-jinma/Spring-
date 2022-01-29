package jp.ken.jdbc.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import jp.ken.jdbc.dao.MembersDao;
import jp.ken.jdbc.entity.Members;
import jp.ken.jdbc.groups.GroupOrder;
import jp.ken.jdbc.model.MembersModel;
import jp.ken.jdbc.model.MembersSearchModel;

@Controller
public class JdbcMembersController {
    @Autowired
    private MembersDao membersDao;

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String toSearch(Model model) {
        model.addAttribute("membersSearchModel", new MembersSearchModel());
        model.addAttribute("headline", "会員検索" );
        return "membersSearch";
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String searchMembers(@ModelAttribute MembersSearchModel membersSearchModel, Model model) {
        // IDと氏名が未入力か判定
        boolean idIsEmpty = membersSearchModel.getId().isEmpty();
        boolean nameIsEmpty = membersSearchModel.getName().isEmpty();

        // IDと氏名ともに未入力の場合
        if(idIsEmpty && nameIsEmpty) {
            //全件検索
            List<Members> membersList = membersDao.getList();
            model.addAttribute("membersList", membersList);

        // IDだけが入力された場合
        } else if(!idIsEmpty && nameIsEmpty) {
            try {
                //ID検索
                Integer id = new Integer(membersSearchModel.getId());
                Members members = membersDao.getMembersById(id);

                if (members == null) {
                    model.addAttribute("message", "該当データはありません");
                } else {
                    List<Members> membersList = new ArrayList<Members>();
                    membersList.add(members);
                    model.addAttribute("membersList", membersList);
                }
            } catch (NumberFormatException e) {
                model.addAttribute("message", "IDが不正です");
            }

        // 氏名だけが入力された場合
        } else if(idIsEmpty && !nameIsEmpty) {
            // 氏名検索(あいまい検索)
            List<Members> membersList = membersDao.getListByName(membersSearchModel.getName());

            if (membersList.isEmpty()) {
                model.addAttribute("message", "該当データはありません");
            } else {
                model.addAttribute("membersList", membersList);
            }

        // IDと氏名ともに入力された場合
        } else {
            model.addAttribute("message", "IDまたは氏名のいずれかを入力してください");
        }

        model.addAttribute("headline", "会員検索" );
        return "membersSearch";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String toRegistration(Model model) {
        model.addAttribute("membersModel", new MembersModel());
        model.addAttribute("headline","会員登録" );
        return "membersRegistration";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String registMembers(Model model,
            @Validated(GroupOrder.class) @ModelAttribute MembersModel membersModel,
            BindingResult result) {
        if(result.hasErrors()) {
            model.addAttribute("headline", "会員登録" );
            return "membersRegistration";
        }

        Members members = new Members();
        members.setName(membersModel.getName());
        members.setEmail(membersModel.getEmail());
        members.setPhoneNumber(membersModel.getPhoneNumber());
        members.setBirthday(Members.parseDate(membersModel.getBirthday()));

        int numberOfRow = membersDao.insert(members);
        if (numberOfRow == 0){
            model.addAttribute("message", "登録に失敗しました。");
            model.addAttribute("headline", "会員登録" );
            return "membersRegistration";
        }

        return "redirect:/complete";
    }

    @RequestMapping(value = "/complete", method = RequestMethod.GET)
    public String toComplete(Model model) {
        model.addAttribute("headline", "会員登録完了" );
        return "membersRegistrationComplete";
    }

    @RequestMapping(value="/edit", method=RequestMethod.GET)
    public String toEdit(@RequestParam(value="id2", defaultValue = "") String id, Model model) {

        Members members = membersDao.getMembersById( new Integer(id));

        MembersModel membersModel = new MembersModel();
        membersModel.setName( members.getName() );
        membersModel.setEmail( members.getEmail() );
        membersModel.setPhoneNumber( members.getPhoneNumber() );
        membersModel.setBirthday( members.getBirthday().toString().replace("-", "/") );

    	model.addAttribute("membersModel", membersModel);
    	model.addAttribute("id", id );
    	model.addAttribute("headline", "会員更新" );

    	return "membersEdit";
    }

    @RequestMapping(value="/edit", method=RequestMethod.POST)
    public String toEdit(@RequestParam(value="id") String id,
    		Model model,
            @Validated(GroupOrder.class) @ModelAttribute MembersModel membersModel,
            BindingResult result) {

    	if( result.hasErrors() ) {
    		model.addAttribute("headline", "会員更新完了" );
    		return "membersEdit";
    	}

    	//更新処理
        Members members = new Members();
        members.setName(membersModel.getName());
        members.setEmail(membersModel.getEmail());
        members.setPhoneNumber(membersModel.getPhoneNumber());
        members.setBirthday(Members.parseDate(membersModel.getBirthday()));

        int numberOfRow = membersDao.update(members, id);
        if (numberOfRow == 0){
            model.addAttribute("message", "更新に失敗しました。");
            model.addAttribute("headline", "会員更新" );
            return "membersEdit";
        }

    	model.addAttribute("membersModel", membersModel);
    	model.addAttribute("id", id);
    	model.addAttribute("headline", "会員更新完了" );
    	model.addAttribute("message", "会員情報を更新しました");

    	return "membersEdit";
    }

    @RequestMapping(value="delete", method = RequestMethod.GET)
    public String delete(@RequestParam("delid") String delid, Model model ) {

    	//削除処理
        int numberOfRow = membersDao.delete( delid );
        if (numberOfRow == 0){
            model.addAttribute("message", "削除に失敗しました。");
            model.addAttribute("headline", "会員検索" );
            return "membersSearch";
        }

        return "redirect:/search";
    }

}