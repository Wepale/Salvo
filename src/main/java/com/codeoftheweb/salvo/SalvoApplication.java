package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@SpringBootApplication
public class SalvoApplication {

	private static final String PATROL_BOAT = "patrolBoat";
	private static final String BATTLESHIP = "battleship";
	private static final String SUBMARINE = "submarine";
	private static final String CARRIER = "carrier";
	private static final String DESTROYER = "destroyer";

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	// PLAYERS
	Player player1 = new Player("j.bauer@ctu.gov", "24");
	Player player2 = new Player("c.obrian@ctu.gov", "42");
	Player player3 = new Player("kim_bauer@gmail.com","kb");
	Player player4 = new Player("t.almeida@ctu.gov","mole");

	//GAMES
	Game game1 = new Game(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
	Game game2 = new Game(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now().plusSeconds(3600)));
	Game game3 = new Game(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now().plusSeconds(7200)));
	Game game4 = new Game(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now().plusSeconds(8000)));
	Game game5 = new Game(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now().plusSeconds(9000)));
	Game game6 = new Game(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now().plusSeconds(10000)));
	Game game7 = new Game(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now().plusSeconds(11000)));
	Game game8 = new Game(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now().plusSeconds(12000)));

	//GAMEPLAYERS
	GamePlayer gameP1 = new GamePlayer(player1, game1); //j.bauer
	GamePlayer gameP2 = new GamePlayer(player2, game1); // c.obrian
	GamePlayer gameP3 = new GamePlayer(player1, game2); // j.bauer
	GamePlayer gameP4 = new GamePlayer(player2, game2); // c.obrian
	GamePlayer gameP5 = new GamePlayer(player1, game3); // c.obrian
	GamePlayer gameP6 = new GamePlayer(player4, game3); // t.almeida
	GamePlayer gameP7 = new GamePlayer(player2, game4); // c.obrian
	GamePlayer gameP8 = new GamePlayer(player1, game4); // j.bauer
	GamePlayer gameP9 = new GamePlayer(player4, game5); // t.almeida
	GamePlayer gameP10 = new GamePlayer(player1, game5); // j.bauer
	GamePlayer gameP11 = new GamePlayer(player3, game6); // kim_bauer
	GamePlayer gameP12 = new GamePlayer(player4, game7); // t.almeida
	GamePlayer gameP13 = new GamePlayer(player3, game8); // kim_bauer
	GamePlayer gameP14 = new GamePlayer(player4, game8); // t.almeida

	//SHIPS
		// Game 1
	Ship ship1 = new Ship(DESTROYER, Arrays.asList("H2", "H3", "H4")); //j.bauer
	Ship ship2 = new Ship(SUBMARINE, Arrays.asList("E1", "F1", "G1")); //j.bauer
	Ship ship3 = new Ship(PATROL_BOAT, Arrays.asList("B4", "B5")); //j.bauer
	Ship ship4 = new Ship(DESTROYER, Arrays.asList("B5", "C5", "D5")); //c.obrian
	Ship ship5 = new Ship(PATROL_BOAT, Arrays.asList("F1", "F2")); //c.obrian

		//Game 2
	Ship ship6 = new Ship(DESTROYER, Arrays.asList("B5", "C5", "D5")); //j.bauer
	Ship ship7 = new Ship(PATROL_BOAT, Arrays.asList("C6", "C7")); //j.bauer
	Ship ship8 = new Ship(SUBMARINE, Arrays.asList("A2", "A3", "A4")); //c.obrian
	Ship ship9 = new Ship(PATROL_BOAT, Arrays.asList("G6", "H6")); //c.obrian
		//Game 3
	Ship ship10 = new Ship(DESTROYER, Arrays.asList("B5", "C5", "D5")); //c.obrian
	Ship ship11 = new Ship(PATROL_BOAT, Arrays.asList("C6", "C7")); //c.obrian
	Ship ship12 = new Ship(SUBMARINE, Arrays.asList("A2", "A3", "A4")); //t.almeida
	Ship ship13 = new Ship(PATROL_BOAT, Arrays.asList("G6", "H6")); //t.almeida
		//Game 4
	Ship ship14 = new Ship(DESTROYER, Arrays.asList("B5", "C5", "D5")); //c.obrian
	Ship ship15 = new Ship(PATROL_BOAT, Arrays.asList("C6", "C7")); //c.obrian
	Ship ship16 = new Ship(SUBMARINE, Arrays.asList("A2", "A3", "A4")); //j.bauer
	Ship ship17 = new Ship(PATROL_BOAT, Arrays.asList("G6", "H6")); //j.bauer
		//Game 5
	Ship ship18 = new Ship(DESTROYER, Arrays.asList("B5", "C5", "D5")); //t.almeida
	Ship ship19 = new Ship(PATROL_BOAT, Arrays.asList("C6", "C7")); //t.almeida
	Ship ship20 = new Ship(SUBMARINE, Arrays.asList("A2", "A3", "A4")); //j.bauer
	Ship ship21 = new Ship(PATROL_BOAT, Arrays.asList("G6", "H6")); //j.bauer
		//Game 6
	Ship ship22 = new Ship(DESTROYER, Arrays.asList("B5", "C5", "D5")); //kim_bauer
	Ship ship23 = new Ship(PATROL_BOAT, Arrays.asList("C6", "C7")); //kim_bauer
		//Game 7
	//No ships
		// Game 8
	Ship ship24 = new Ship(DESTROYER, Arrays.asList("B5", "C5", "D5")); //kim_bauer
	Ship ship25 = new Ship(PATROL_BOAT, Arrays.asList("C6", "C7")); //kim_bauer
	Ship ship26 = new Ship(SUBMARINE, Arrays.asList("A2", "A3", "A4")); //t.almeida
	Ship ship27 = new Ship(PATROL_BOAT, Arrays.asList("G6", "H6")); //t.almeida




	//Game1
	Salvo salvo1 = new Salvo(1, Arrays.asList("B5", "C5", "F1")); //j.bauer
	Salvo salvo2 = new Salvo(1, Arrays.asList("B4", "B5", "B6")); //c.obrian
	Salvo salvo3 = new Salvo(2, Arrays.asList("F2", "D5")); //j.bauer
	Salvo salvo4 = new Salvo(2, Arrays.asList("E1", "H3", "A2")); //c.obrian
	//Game2
	Salvo salvo5 = new Salvo(1, Arrays.asList("A2", "A4", "G6")); //j.bauer
	Salvo salvo6 = new Salvo(1, Arrays.asList("B5", "D5", "C7")); //c.obrian
	Salvo salvo7 = new Salvo(2, Arrays.asList("A3", "H6")); //j.bauer
	Salvo salvo8 = new Salvo(2, Arrays.asList("C5", "C6")); //c.obrian
	//Game3
	Salvo salvo9 = new Salvo(1, Arrays.asList("G6", "H6", "A4")); //c.obrian
	Salvo salvo10 = new Salvo(1, Arrays.asList("H1", "H2", "H3")); //t.almeida
	Salvo salvo11 = new Salvo(2, Arrays.asList("A2", "A3", "D8")); //c.obrian
	Salvo salvo12 = new Salvo(2, Arrays.asList("E1", "F2", "G3")); //t.almeida
	//Game4
	Salvo salvo13 = new Salvo(1, Arrays.asList("A3", "A4", "F7")); //c.obrian
	Salvo salvo14 = new Salvo(1, Arrays.asList("B5", "C6", "H1")); //j.bauer
	Salvo salvo15 = new Salvo(2, Arrays.asList("A2", "G6", "H6")); //c.obrian
	Salvo salvo16 = new Salvo(2, Arrays.asList("C5", "C7", "D5")); //j.bauer
	//Game5
	Salvo salvo17 = new Salvo(1, Arrays.asList("A1", "A2", "A3")); //t.almeida
	Salvo salvo18 = new Salvo(1, Arrays.asList("B5", "B6", "C7")); //j.bauer
	Salvo salvo19 = new Salvo(2, Arrays.asList("G6", "G7", "G8")); //t.almeida
	Salvo salvo20 = new Salvo(2, Arrays.asList("C6", "D6", "E6")); //j.bauer
	Salvo salvo21 = new Salvo(3, Arrays.asList("H1", "H8")); //j.bauer


	Score score1 = new Score(1.0, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
	Score score2 = new Score(0.0, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
	Score score3 = new Score(0.5, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
	Score score4 = new Score(0.5, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
	Score score5 = new Score(1.0, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
	Score score6 = new Score(0.0, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
	Score score7 = new Score(0.5, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
	Score score8 = new Score(0.5, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));

	@SuppressWarnings("Duplicates")
	@Bean
	public CommandLineRunner initData(PlayerRepository pRepository,
									  GameRepository gRepository,
									  GamePlayerRepository gpRepository,
									  ShipRepository shipRepository,
									  SalvoRepository salvoRepository,
									  ScoreRepository scoreRepo) {
		return (args) -> {
			gameP1.getScore();
			//Game1
			player1.addScore(score1);
			player2.addScore(score2);
			game1.addScore(score1);
			game1.addScore(score2);
			//Game2
			game2.addScore(score3);
			game3.addScore(score4);
			player1.addScore(score3);
			player2.addScore(score4);
			//game3
			game3.addScore(score5);
			game3.addScore(score6);
			player2.addScore(score5);
			player4.addScore(score6);
			//game4
			game4.addScore(score7);
			game4.addScore(score8);
			player2.addScore(score7);
			player1.addScore(score8);


			//Game 1
			gameP1.addShip(ship1);
			gameP1.addShip(ship2);
			gameP1.addShip(ship3);
			gameP2.addShip(ship4);
			gameP2.addShip(ship5);
			//Game2
			gameP3.addShip(ship6);
			gameP3.addShip(ship7);
			gameP4.addShip(ship8);
			gameP4.addShip(ship9);
			//Game3
			gameP5.addShip(ship10);
			gameP5.addShip(ship11);
			gameP6.addShip(ship12);
			gameP6.addShip(ship13);
			//Game4
			gameP7.addShip(ship14);
			gameP7.addShip(ship15);
			gameP8.addShip(ship16);
			gameP8.addShip(ship17);
			//Game5
			gameP9.addShip(ship18);
			gameP9.addShip(ship19);
			gameP10.addShip(ship20);
			gameP10.addShip(ship21);
			// Game6
			gameP11.addShip(ship22);
			gameP11.addShip(ship23);
			//Game7
			//No ships
			//Game 8
			gameP13.addShip(ship24);
			gameP13.addShip(ship25);
			gameP14.addShip(ship26);
			gameP14.addShip(ship27);

			//Game 1
			gameP1.addSalvo(salvo1);
			gameP2.addSalvo(salvo2);
			gameP1.addSalvo(salvo3);
			gameP2.addSalvo(salvo4);
			//Game2
			gameP3.addSalvo(salvo5);
			gameP4.addSalvo(salvo6);
			gameP3.addSalvo(salvo7);
			gameP4.addSalvo(salvo8);
			//Game3
			gameP5.addSalvo(salvo9);
			gameP6.addSalvo(salvo10);
			gameP5.addSalvo(salvo11);
			gameP6.addSalvo(salvo12);
			//Game4
			gameP7.addSalvo(salvo13);
			gameP8.addSalvo(salvo14);
			gameP7.addSalvo(salvo15);
			gameP8.addSalvo(salvo16);
			//Game5
			gameP9.addSalvo(salvo17);
			gameP10.addSalvo(salvo18);
			gameP9.addSalvo(salvo19);
			gameP10.addSalvo(salvo20);
			gameP10.addSalvo(salvo21);

			pRepository.save(player1);
			pRepository.save(player2);
			pRepository.save(player3);
			pRepository.save(player4);

			gRepository.save(game1);
			gRepository.save(game2);
			gRepository.save(game3);
			gRepository.save(game4);
			gRepository.save(game5);
			gRepository.save(game6);
			gRepository.save(game7);
			gRepository.save(game8);

			gpRepository.save(gameP1);
			gpRepository.save(gameP2);
			gpRepository.save(gameP3);
			gpRepository.save(gameP4);
			gpRepository.save(gameP5);
			gpRepository.save(gameP6);
			gpRepository.save(gameP7);
			gpRepository.save(gameP8);
			gpRepository.save(gameP9);
			gpRepository.save(gameP10);
			gpRepository.save(gameP11);
			gpRepository.save(gameP12);
			gpRepository.save(gameP13);
			gpRepository.save(gameP14);

			shipRepository.save(ship1);
			shipRepository.save(ship2);
			shipRepository.save(ship3);
			shipRepository.save(ship4);
			shipRepository.save(ship5);
			shipRepository.save(ship6);
			shipRepository.save(ship7);
			shipRepository.save(ship8);
			shipRepository.save(ship9);
			shipRepository.save(ship10);
			shipRepository.save(ship11);
			shipRepository.save(ship12);
			shipRepository.save(ship13);
			shipRepository.save(ship14);
			shipRepository.save(ship15);
			shipRepository.save(ship16);
			shipRepository.save(ship17);
			shipRepository.save(ship18);
			shipRepository.save(ship19);
			shipRepository.save(ship20);
			shipRepository.save(ship21);
			shipRepository.save(ship22);
			shipRepository.save(ship23);
			shipRepository.save(ship24);
			shipRepository.save(ship25);
			shipRepository.save(ship26);
			shipRepository.save(ship27);

			salvoRepository.save(salvo1);
			salvoRepository.save(salvo2);
			salvoRepository.save(salvo3);
			salvoRepository.save(salvo4);
			salvoRepository.save(salvo5);
			salvoRepository.save(salvo6);
			salvoRepository.save(salvo7);
			salvoRepository.save(salvo8);
			salvoRepository.save(salvo9);
			salvoRepository.save(salvo10);
			salvoRepository.save(salvo11);
			salvoRepository.save(salvo12);
			salvoRepository.save(salvo13);
			salvoRepository.save(salvo14);
			salvoRepository.save(salvo15);
			salvoRepository.save(salvo16);
			salvoRepository.save(salvo17);
			salvoRepository.save(salvo18);
			salvoRepository.save(salvo19);
			salvoRepository.save(salvo20);
			salvoRepository.save(salvo21);

			scoreRepo.save(score1);
			scoreRepo.save(score2);
			scoreRepo.save(score3);
			scoreRepo.save(score4);
			scoreRepo.save(score5);
			scoreRepo.save(score6);
			scoreRepo.save(score7);
			scoreRepo.save(score8);

			gameP1.getScore();
			gameP2.getScore();

		};
	}
}


@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

	@Autowired
	PlayerRepository playerRepo;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(inputName -> playerRepo.findByUserName(inputName)
				.map(player -> new User(player.getUserName(), player.getPassword(),
						AuthorityUtils.createAuthorityList("USER")))
				.orElseThrow(() -> new UsernameNotFoundException("Unknown user: " + inputName)));
	}
}

@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
//				.antMatchers("/rest/**").denyAll()
				.antMatchers("/api/games",
						"/api/players",
						"/api/games/**",
						"/web/**",
						"/favicon.ico",
                        "/rest/**")
				.permitAll()
				.anyRequest().hasAuthority("USER");

		http.formLogin()
				.loginPage("/api/login")
				.usernameParameter("username")
				.passwordParameter("password");

		http.logout()
				.logoutUrl("/api/logout");

		// turn off checking for CSRF tokens
		http.csrf().disable();

		// if user is not authenticated, just send an authentication failure response
		http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if login is successful, just clear the flags asking for authentication
		http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

		// if login fails, just send an authentication failure response
		http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if logout is successful, just send a success response
		http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
	}

	private void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		}
	}
}

