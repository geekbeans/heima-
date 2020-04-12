package cn.itcast.travel.web.servlet;

import cn.itcast.travel.domain.ResultInfo;
import cn.itcast.travel.domain.User;
import cn.itcast.travel.service.UserService;
import cn.itcast.travel.service.impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.beanutils.BeanUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@WebServlet("/user/*")
/**
 * 注册功能
 */

public class UserServlet extends BaseServlet {
    //申明UserService的业务对象
    private UserService service = new UserServiceImpl();
    public void regist(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //验证码校验
        String check = request.getParameter("check");
        //从session中获取验证码
        HttpSession session = request.getSession();
        String checkcode_server = (String)session.getAttribute("CHECKCODE_SERVER");
        //为了保证验证码不被重复使用
        session.removeAttribute("CHECKCODE_SERVER");
        //比较验证码是否正确
        if(checkcode_server == null || !checkcode_server.equalsIgnoreCase(check)){
            //如果验证码错误
            ResultInfo info = new ResultInfo();
            //注册失败
            info.setFlag(false);
            info.setErrorMsg("验证码错误");
            //数据响应回客户端
            //将info对象序列化json
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(info);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(json);
            return;
        }

        //获取数据
        Map<String,String[]> map = request.getParameterMap();
        //封装对象
        User user = new User();
        try {
            BeanUtils.populate(user,map);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        //调用service完成注册
        //UserService service = new UserServiceImpl();
        boolean flag = service.regist(user);
        ResultInfo info = new ResultInfo();
        //响应结果
        if(flag){
            //注册成功
            info.setFlag(true);
        }else{
            //注册失败
            info.setFlag(false);
            info.setErrorMsg("注册失败");
        }
        //将info对象序列化json
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(info);
        //将json数据写回客户端
        //设置content-type
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(json);
    }

    //登录方法
    public void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //获取用户名和密码数据
        Map<String,String[]> map = request.getParameterMap();
        //封装user对象
        User user = new User();
        try {
            BeanUtils.populate(user,map);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        //调用service查询
       // UserService service = new UserServiceImpl();
        User u = service.login(user);
        ResultInfo info = new ResultInfo();
        //判断用户对象是否为null
        if(u == null){
            //用户名密码错误
            info.setFlag(false);
            info.setErrorMsg("用户名或密码错误");
        }
        //判断用户是否激活
        if(u != null && !"Y".equals(u.getStatus())){
            //表示用户还未激活
            info.setFlag(false);
            info.setErrorMsg("您尚未激活，请先注册激活。");
        }

        //判定用户激活
        if(u != null && "Y".equals(u.getStatus())){
            //表示用户登陆成功
            info.setFlag(true);
        }

        //响应数据并且json序列号
        ObjectMapper mapper = new ObjectMapper();

        response.setContentType("application/json;charset=utf-8");
        mapper.writeValue(response.getOutputStream(),info);
        //此处注意！你要在loginServlet里面的登陆成功后,用session存数据,在header.html里面的是user.username,是User类(数据库)的username,这样在index页面就会有了
        request.getSession().setAttribute("user", user);
    }

    //查找用户方法
    public void findOne(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //从session中获取登录用户
        Object user = request.getSession().getAttribute("user");
        //将user写回客户端
//        ObjectMapper mapper = new ObjectMapper();
//        response.setContentType("application/json;charset=utf-8");
//        mapper.writeValue(response.getOutputStream(),user);
        writeValue(user,response);
    }

    //退出方法
    public void exit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //销毁session,即退出
        request.getSession().invalidate();
        //跳转到登录页面
        response.sendRedirect(request.getContextPath()+"/login.html");
    }

    //激活用户方法
    public void active(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //获取激活码
        String code = request.getParameter("code");
        if (code != null) {
            //调用service完成激活操作
           // UserService service = new UserServiceImpl();
            boolean flag = service.active(code);

            //判断标记
            String msg = null;
            if (flag) {
                //激活成功
                msg = "激活成功！请<a href='login.html'>登录</a>";

            } else {
                //激活失败
                msg = "激活失败，请重试";
            }
            response.setContentType("text/html;charset = utf-8");
            response.getWriter().write(msg);
        }
    }

}
