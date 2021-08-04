package com.nowcoder.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @ResponseBody
    @RequestMapping("/hello")
    public String sayHello() {
        return "Hello Spring Boot!";
    }

    // Handle request
    @RequestMapping("/http")
    public void httpRequest(HttpServletRequest request, HttpServletResponse response) {
        // get request
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> enumeration = request.getHeaderNames();
        while(enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + ": " + value);
        }
        System.out.println(request.getParameter("code"));

        // return response
        response.setContentType("text/html;charset=utf-8");
        try{
            PrintWriter printWriter = response.getWriter();
            printWriter.write("<h1>Hello!</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(path="/students", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            @RequestParam(name="current", required = false) int current,
            @RequestParam(name="limit", required = false) int limit) {
        System.out.println(current);
        System.out.println(limit);
        return "Some students";
    }

    @RequestMapping(path="/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id) {
        System.out.println(id);
        return "A student";
    }

    //Post request
    @RequestMapping(path = "/student", method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return "Success";
    }

    // 相应HTML
    @RequestMapping(path="/teacher", method = RequestMethod.GET)
    public ModelAndView getTeacher() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("name", "Tim");
        mav.addObject("age", "55");
        mav.setViewName("/demo/view");
        return mav;
    }

    @RequestMapping(path="/school", method = RequestMethod.GET)
    public String getSchool(Model model) {
        model.addAttribute("name", "SPBSTU");
        model.addAttribute("age", "122");
        return "/demo/view";
    }

    // 相应JSON（异步请求中）
    @RequestMapping(path = "/emp", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getEmp() {
        Map<String, Object> emp = new HashMap<>();
        emp.put("name", "Tim");
        emp.put("age", 55);
        return emp;
    }

    @RequestMapping(path = "/emps", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getEmps() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> emp = new HashMap<>();
        emp.put("name", "Tim");
        emp.put("age", 55);
        list.add(emp);
        emp = new HashMap<>();
        emp.put("name", "Cooker");
        emp.put("age", 55);
        list.add(emp);

        return list;
    }
}
