package com.example.ExamSys.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ExamSys.dao.StudentRepository;
import com.example.ExamSys.dao.TeacherRepository;
import com.example.ExamSys.dao.UserRepository;
import com.example.ExamSys.domain.Authority;
import com.example.ExamSys.domain.Student;
import com.example.ExamSys.domain.Teacher;
import com.example.ExamSys.domain.User;
import com.example.ExamSys.domain.enumeration.UserType;
import com.example.ExamSys.security.AuthoritiesConstants;
import com.example.ExamSys.service.ProductionService;
import com.example.ExamSys.service.UserService;
import com.example.ExamSys.service.VerifyService;
import com.example.ExamSys.utils.MD5Utils;
import com.example.ExamSys.vo.EmailUserDTO;
import com.example.ExamSys.vo.PhoneUserDTO;
import com.example.ExamSys.vo.UserInfoDTO;

@RestController
@RequestMapping("/account")
public class AccountController {
	private final Logger log = LoggerFactory.getLogger(AccountController.class);
	
	private static final String KEY = "examsys";
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private VerifyService verifyService;
	
	@Autowired
	private StudentRepository studentRepository;
	
	@Autowired
	private TeacherRepository teacherRepository;
	
	@Autowired
	private ProductionService productionService;
	
	
	/**
	 * 用户输出收到的验证码，进行邮箱注册
	 * 参数: UserDTO
	 * 		hash:发送验证码后，返回的hash值
	 * 		time:发送验证码后，返回的time值
	 * 		verificationCode: 用户输入的验证码
	 * 返回值: ResponseEntity 状态
	 */
	// /register->/registeremail,userDTO新增手机号字段
	@RequestMapping(value = "/registeremail", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<String> registerAccountEmail(@Valid @RequestBody EmailUserDTO userDTO, 
			@RequestParam(value = "hash") String hash, @RequestParam(value = "time") String time, @RequestParam(value = "verificationCode") String verificationCode){
		
		HttpHeaders textPlainHeaders = new HttpHeaders();
		
		String currentTime = verifyService.getCurrentTime();
		String hashNow = MD5Utils.getMD5Code(KEY + "@" + time + "@" + verificationCode);
		if(time.compareTo(currentTime) > 0) {
			if(hash.equalsIgnoreCase(hashNow)) {
				//校验成功
				return userRepository.findOneByLogin(userDTO.getLogin())
						.map(user -> new ResponseEntity<>("login already in use", textPlainHeaders, HttpStatus.BAD_REQUEST))
						.orElseGet(() -> userRepository.findOneByEmail(userDTO.getEmail())
								.map(user -> new ResponseEntity<>("email already in use", textPlainHeaders, HttpStatus.BAD_REQUEST))
								.orElseGet(() -> {
									if(userDTO.getUserType().equals(UserType.STUDENT)) {
										User user = userService.createUserEmail(userDTO.getLogin(),
												userDTO.getPassword(),
												userDTO.getEmail(),
												true);
										Set<Authority> authorities = new HashSet<Authority>();
										authorities.add(new Authority(AuthoritiesConstants.STUDENT));
										user.setAuthorities(authorities);
										userRepository.save(user);
										Student student = new Student();
										student.setUser(user);
										studentRepository.save(student);
									} else {
										User user = userService.createUserEmail(userDTO.getLogin(),
												userDTO.getPassword(),
												userDTO.getEmail(),
												false);
										Set<Authority> authorities = new HashSet<Authority>();
										authorities.add(new Authority(AuthoritiesConstants.TEACHER));
										user.setAuthorities(authorities);
										userRepository.save(user);
										Teacher teacher = new Teacher();
										teacher.setUser(user);
										teacherRepository.save(teacher);
									}
									return new ResponseEntity<>(HttpStatus.CREATED);
								})
								);
			} else {
				//验证码不正确，校验失败
				return new ResponseEntity<>("verify incorrect", textPlainHeaders, HttpStatus.BAD_REQUEST);
			}
		} else {
			// 超时
			return new ResponseEntity<>("verify time out", textPlainHeaders, HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * 用户输出收到的验证码，进行手机号注册
	 * 参数: UserDTO
	 * 		hash:发送验证码后，返回的hash值
	 * 		time:发送验证码后，返回的time值
	 * 		verificationCode: 用户输入的验证码
	 * 返回值: ResponseEntity 状态
	 */
	// 在student或者teacher里增加一条初始信息
	@RequestMapping(value = "/registerphone", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<String> registerAccountPhone(@Valid @RequestBody PhoneUserDTO userDTO, 
			@RequestParam(value = "hash") String hash, @RequestParam(value = "time") String time, @RequestParam(value = "verificationCode") String verificationCode){
		
		HttpHeaders textPlainHeaders = new HttpHeaders();
		
		String currentTime = verifyService.getCurrentTime();
		String hashNow = MD5Utils.getMD5Code(KEY + "@" + time + "@" + verificationCode);
		if(time.compareTo(currentTime) > 0) {
			if(hash.equalsIgnoreCase(hashNow)) {
				//校验成功
				return userRepository.findOneByLogin(userDTO.getLogin())
						.map(user -> new ResponseEntity<>("login already in use", textPlainHeaders, HttpStatus.BAD_REQUEST))
						.orElseGet(() -> userRepository.findOneByPhoneNumber(userDTO.getPhoneNumber())
								.map(user -> new ResponseEntity<>("phoneNumber already in use", textPlainHeaders, HttpStatus.BAD_REQUEST))
								.orElseGet(() -> {
									if(userDTO.getUserType().equals(UserType.STUDENT)) {
										User user = userService.createUserPhone(userDTO.getLogin(),
												userDTO.getPassword(),
												userDTO.getPhoneNumber(),
												true);
										Set<Authority> authorities = new HashSet<Authority>();
										authorities.add(new Authority(AuthoritiesConstants.STUDENT));
										user.setAuthorities(authorities);
										userRepository.save(user);
										Student student = new Student();
										student.setUser(user);
										studentRepository.save(student);
									} else {
										User user = userService.createUserPhone(userDTO.getLogin(),
												userDTO.getPassword(),
												userDTO.getPhoneNumber(),
												false);
										Set<Authority> authorities = new HashSet<Authority>();
										authorities.add(new Authority(AuthoritiesConstants.TEACHER));
										user.setAuthorities(authorities);
										userRepository.save(user);
										Teacher teacher = new Teacher();
										teacher.setUser(user);
										teacherRepository.save(teacher);
									}
									return new ResponseEntity<>(HttpStatus.CREATED);
								})
								);
			} else {
				//验证码不正确，校验失败
				return new ResponseEntity<>("verify incorrect", textPlainHeaders, HttpStatus.BAD_REQUEST);
			}
		} else {
			// 超时
			return new ResponseEntity<>("verify time out", textPlainHeaders, HttpStatus.BAD_REQUEST);
		}
	}
	
	
	/**
	 * 忘记密码时，验证验证码
	 */
	@RequestMapping(value = "/verifyforget", method = RequestMethod.POST)
	public ResponseEntity<String> verifyForget(@RequestParam(value = "hash") String hash, 
			@RequestParam(value = "time") String time, @RequestParam(value="verificationCode") String verificationCode){
		
		HttpHeaders textPlainHeaders = new HttpHeaders();
		
		String currentTime = verifyService.getCurrentTime();
		String hashNow = MD5Utils.getMD5Code(KEY + "@" + time + "@" + verificationCode);
		if(time.compareTo(currentTime) > 0) {
			if(hash.equalsIgnoreCase(hashNow)) {
				return ResponseEntity.ok().body("");
			} else {
				return new ResponseEntity<>("verify incorrect", textPlainHeaders, HttpStatus.BAD_REQUEST);
			}
		} else {
			return new ResponseEntity<>("verify time out", textPlainHeaders, HttpStatus.BAD_REQUEST);
		}
	}
		
	/**
	 * 发送邮箱验证码
	 * 超时时间：5分钟
	 * 验证码位数：6位数
	 * 参数: email 邮箱
	 * 返回值：hash:密码密文
	 * 		time:时间字符串
	 */
	@RequestMapping(value = "/sendmessageemail", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Map<String, Object>> sendMessage(@RequestParam(value = "email") String email){
		
		Optional<User> userOpt = userRepository.findOneByEmail(email);
		if(userOpt.isPresent()) {
			return ResponseEntity.badRequest().header("Email", "The mailbox is occupied").body(null);
		}
		
		String randomNum = "1000000";
		String currentTime = verifyService.getFiveMinuteTime();
		try {
			randomNum = verifyService.getAndSendVerify(email);
		} catch(Exception e) {
			log.info("{}短信验证码发送出错", email);
			System.out.println(e);
			return ResponseEntity.badRequest().header("Email", "Verification code send failed").body(null);
			
		}
		String hash = MD5Utils.getMD5Code(KEY + "@" + currentTime + "@" + randomNum);
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("hash", hash);
		resultMap.put("time", currentTime);
		return ResponseEntity.ok().body(resultMap);
	}
	
	/**
	 * 忘记密码时，发送邮箱验证码
	 * 参数：email 邮箱
	 * 返回值：hash: 密文
	 * 		 time: 时间
	 */
	@RequestMapping(value = "/sendmessageforgetemail", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> sendMessageForget(@RequestParam(value = "email") String email){
		
		Optional<User> userOpt = userRepository.findOneByEmail(email);
		if(!userOpt.isPresent()) {
			return ResponseEntity.badRequest().header("Email", "The mailbox not be registered").body(null);
		}
		String randomNum = "1000000";
		String currentTime = verifyService.getFiveMinuteTime();
		try {
			randomNum = verifyService.getAndSendVerify(email);
		} catch(Exception e) {
			log.info("{}忘记密码短信验证码发送出错", email);
			return ResponseEntity.badRequest().header("Email", "Verification code send failed").body(null);
			
		}
		String hash = MD5Utils.getMD5Code(KEY + "@" + currentTime + "@" + randomNum);
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("hash", hash);
		resultMap.put("time", currentTime);
		return ResponseEntity.ok().body(resultMap);
	}
	
	/**
	 * 发送手机验证码
	 * 超时时间：5分钟
	 * 验证码位数：6位数
	 * 参数: phone 手机
	 * 返回值：hash:密码密文
	 * 		time:时间字符串
	 */
	@RequestMapping(value = "/sendmessagephone", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Map<String, Object>> sendMessagePhone(@RequestParam(value = "phoneNumber") String phoneNumber){
		
		Optional<User> userOpt = userRepository.findOneByPhoneNumber(phoneNumber);
		if(userOpt.isPresent()) {
			return ResponseEntity.badRequest().header("PhoneNumber", "The PhoneNumber is occupied").body(null);
		}
		
		String currentTime = verifyService.getFiveMinuteTime();
		HashMap<Boolean, String> response = verifyService.getAndSendVerifyMessage(phoneNumber);
		if(response.get(true) != null) {
			String hash = MD5Utils.getMD5Code(KEY + "@" + currentTime + "@" + response.get(true));
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("hash", hash);
			resultMap.put("time", currentTime);
			return ResponseEntity.ok().body(resultMap);
		}
		else if(response.get(false) != null) {
			log.info("{}手机验证码发送出错，错误信息：{}", phoneNumber, response.get(false));
			return ResponseEntity.badRequest().header("PhoneNumber", response.get(false)).body(null);
		}
		else {
			log.info("{}手机验证码发送出现未知错误", phoneNumber, response.get(false));
			return ResponseEntity.badRequest().header("PhoneNumber", "An unknown error happened").body(null);
		}
	}
	
	/**
	 * 忘记密码时，发送手机验证码
	 * 参数：email 邮箱
	 * 返回值：hash: 密文
	 * 		 time: 时间
	 */
	@RequestMapping(value = "/sendmessageforgetphone", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> sendMessagePhoneForget(@RequestParam(value = "phoneNumber") String phoneNumber){
		
		Optional<User> userOpt = userRepository.findOneByPhoneNumber(phoneNumber);
		if(!userOpt.isPresent()) {
			return ResponseEntity.badRequest().header("PhoneNumber", "The phoneNumber not be registered").body(null);
		}
		
		String currentTime = verifyService.getFiveMinuteTime();
		HashMap<Boolean, String> response = verifyService.getAndSendVerifyMessage(phoneNumber);
		if(response.get(true) != null) {
			String hash = MD5Utils.getMD5Code(KEY + "@" + currentTime + "@" + response.get(true));
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("hash", hash);
			resultMap.put("time", currentTime);
			return ResponseEntity.ok().body(resultMap);
		}
		else if(response.get(false) != null) {
			log.info("{}忘记密码手机验证码发送出错，错误信息：{}", phoneNumber, response.get(false));
			return ResponseEntity.badRequest().header("PhoneNumber", response.get(false)).body(null);
		}
		else {
			log.info("{}忘记密码手机验证码发送出现未知错误", phoneNumber, response.get(false));
			return ResponseEntity.badRequest().header("PhoneNumber", "An unknown error happened").body(null);
		}
	}
	
	/**
	 * 学生个人信息录入
	 * 参数: userId 用户id
	 * 返回值: StudentDTO
	 * username, realname, gender, age, phone, province, city, town, school
	 * trainingagency, motto, personalpic
	 */
	@RequestMapping(value = "/personalInfo", method = RequestMethod.POST)
	public ResponseEntity<String> recordPersonalInfo(@Valid @RequestBody UserInfoDTO userInfoDTO){
		
		Set<Authority> authorities = userRepository.findAuthoritiesByLogin(userInfoDTO.getLogin());
		Boolean bool = true;
		
		if(authorities == null || authorities.isEmpty())
			return ResponseEntity.badRequest().header("Authority", "no Authority").body(null);
		if(authorities.contains(new Authority("ROLE_STUDENT"))) {
		
			Student student = new Student();
			student.setAge(userInfoDTO.getAge());
			student.setGender(userInfoDTO.getGender());
			student.setName(userInfoDTO.getRealname());
			student.setSchool(userInfoDTO.getSchool());
			student.setSchoolProvince(userInfoDTO.getProvince());
			student.setSchoolCity(userInfoDTO.getCity());
			student.setSchoolRegion(userInfoDTO.getTown());
			student.setTrainingName(userInfoDTO.getTrainingAgency());
			student.setMotto(userInfoDTO.getMotto());
			student.setUser(userRepository.findOneByLogin(userInfoDTO.getLogin()).get());
			studentRepository.save(student);
			try {
				File upl = File.createTempFile(userInfoDTO.getLogin() + "_", userInfoDTO.getPersonalpic().getOriginalFilename());
				IOUtils.copy(userInfoDTO.getPersonalpic().getInputStream(), new FileOutputStream(upl));
				
				bool = productionService.upLoadPersonalPhoto(userInfoDTO.getLogin(), upl, "Student");
			} catch (Exception e) {
				e.printStackTrace();
				bool = false;
				return ResponseEntity.badRequest().header("Photo", "Photo save failed").body(null);
			}
			if(bool == false) {
				return ResponseEntity.badRequest().header("Photo", "Photo save failed").body(null);
			}
			return ResponseEntity.ok().body("");
			
		} else if(authorities.contains(new Authority("ROLE_TEACHER"))) {
			
			Teacher teacher = new Teacher();
			teacher.setAge(userInfoDTO.getAge());
			teacher.setGender(userInfoDTO.getGender());
			teacher.setName(userInfoDTO.getRealname());
			teacher.setSchool(userInfoDTO.getSchool());
			teacher.setSchoolProvince(userInfoDTO.getProvince());
			teacher.setSchoolCity(userInfoDTO.getCity());
			teacher.setSchoolRegion(userInfoDTO.getTown());
			teacher.setTrainingName(userInfoDTO.getTrainingAgency());
			teacher.setMotto(userInfoDTO.getMotto());
			teacher.setUser(userRepository.findOneByLogin(userInfoDTO.getLogin()).get());
			teacherRepository.save(teacher);
			try {
				File upl = File.createTempFile(userInfoDTO.getLogin() + "_", userInfoDTO.getPersonalpic().getOriginalFilename());
				IOUtils.copy(userInfoDTO.getPersonalpic().getInputStream(), new FileOutputStream(upl));
				
				bool = productionService.upLoadPersonalPhoto(userInfoDTO.getLogin(), upl, "Teacher");
			} catch (Exception e) {
				e.printStackTrace();
				bool = false;
				return ResponseEntity.badRequest().header("Photo", "Photo save failed").body(null);
			}
			if(bool == false) {
				return ResponseEntity.badRequest().header("Photo", "Photo save failed").body(null);
			}
			return ResponseEntity.ok().body("");
		}
		return ResponseEntity.badRequest().header("Authority", "no Authority Student or Teacher").body(null);
	}	
}
