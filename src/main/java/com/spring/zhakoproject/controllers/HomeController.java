package com.spring.zhakoproject.controllers;

import com.spring.zhakoproject.chat.ChatMessage;
import com.spring.zhakoproject.entities.Categories;
import com.spring.zhakoproject.entities.Gender;
import com.spring.zhakoproject.entities.Items;
import com.spring.zhakoproject.entities.Person;
import com.spring.zhakoproject.services.ItemService;
import com.spring.zhakoproject.services.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {
    @Autowired
    private PersonService personService;

    @Autowired
    private ItemService itemService;
    @GetMapping("/home")
    public String home(){
        return "home";
    }

    @GetMapping("/")
    public String index(){
        return "login";
    }
    @GetMapping("/signIn")
    public String signIn(Model model){
        List<Gender> genders = personService.getAllGender();
        model.addAttribute("genders", genders);
        return "signIn";
    }
    @GetMapping("/test2")
    public String test(Model model){
        List<Person> persons = personService.getAllPersons();
        model.addAttribute("users", persons);

        List<Gender> genders = personService.getAllGender();
        model.addAttribute("genders", genders);
        return "test";
    }

    @GetMapping("/items")
    public String items(){
        return "Items";
    }
    @GetMapping("/shop")
    public String shop(Model model){
        List<Items> items = itemService.getAllItem();
        model.addAttribute("items", items);
        return "shop";
    }
    @GetMapping("/sort")
    public String sort(Model model){
        List<Items> items = itemService.getAllOrdItem();
        model.addAttribute("items", items);
        return "sortedAl";
    }



    @PostMapping("/check")
    public String check(@RequestParam(name = "person_login", defaultValue = "") String login,
                        @RequestParam(name = "person_password", defaultValue = "") String password){

        if (personService.doesPersonExist(login, password)) {
            return "redirect:/home";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("/addItem")
    public String addItem(@RequestParam(name = "id", defaultValue = "0") Long id,
                            @RequestParam(name = "item_name", defaultValue = "no item") String name,
                            @RequestParam(name = "item_price", defaultValue = "0") int price,
                            @RequestParam(name = "item_amount", defaultValue = "0") int amount,
                            @RequestParam(name = "item_info") String info){
        Items items = new Items();
        items.setName(name);
        items.setPrice(price);
        items.setAmount(amount);
        items.setInfo(info);
        itemService.addItem(items);
        return "redirect:/home";
    }
    @GetMapping("/details/{idshka}")
    public String details(Model model, @PathVariable(name="idshka") Long id){
        Items items = itemService.getItem(id);
        model.addAttribute("items", items);

        List<Categories> categories = itemService.getAllCategories();
        model.addAttribute("categories", categories);
        return "details";
    }

    @PostMapping("/addPerson")
    public String addPerson(@RequestParam(name = "id", defaultValue = "0") Long id,
                            @RequestParam(name = "gender_id", defaultValue = "0") Long gender_id,
                            @RequestParam(name = "person_lastname", defaultValue = "who") String lastname,
                            @RequestParam(name = "person_firstname", defaultValue = "who") String firstname,
                            @RequestParam(name = "person_login", defaultValue = "who") String login,
                            @RequestParam(name = "person_password", defaultValue = "") String password){
        Gender g = personService.getGender(gender_id);
        if(g != null){
            Person person = new Person();
            person.setLastname(lastname);
            person.setFirstname(firstname);
            person.setLogin(login);
            person.setPassword(password);
            person.setGender(g);
            personService.addPerson(person);
        }

        return "redirect:/home";
    }

    @PostMapping(value = "/deleteitem")
    public String deleteItem(@RequestParam(name = "id", defaultValue = "0") Long id){
        Items item = itemService.getItem(id);
        if(item != null){
            itemService.deleteItem(item);
        }
        return "redirect:/shop";
    }

    @PostMapping(value = "/reassigncategory")
    public String reAssignCategory(@RequestParam(name = "item_id") Long id,
                                   @RequestParam(name = "category_id") Long categoryId){
        Categories cat = itemService.getCategory(categoryId);
        if(cat != null){
            Items item = itemService.getItem(id);
            if(item!=null){
                List<Categories> categories = item.getCategories();
                categories.remove(cat);
                itemService.saveItem(item);
            }
        }
        return "redirect:/details/" + id;
    }

    @PostMapping(value = "/assigncategory")
    public String assignCategory(@RequestParam(name = "item_id") Long id,
                                 @RequestParam(name = "category_id") Long categoryId){
        Categories cat = itemService.getCategory(categoryId);
        if(cat!=null){
            Items item = itemService.getItem(id);
            if(item!=null){
                List<Categories> categories = item.getCategories();
                if(categories==null){
                    categories = new ArrayList<>();
                }
                categories.add(cat);
                itemService.saveItem(item);

                return "redirect:/details/" + id;
            }
        }
        return "redirect:/";
    }



    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(
            @Payload ChatMessage chatMessage
    ){
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor
    ){
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        return chatMessage;
    }

    @GetMapping("chatting")
    public String chatting(){
        return "chatting";
    }
}
