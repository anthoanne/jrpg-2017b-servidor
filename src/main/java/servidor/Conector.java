package servidor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import comandos.ConexionHibernate;
import mensajeria.PaquetePersonaje;
import mensajeria.PaqueteUsuario;
import modelos.Personaje;
import modelos.Registro;

/**
 * Clase Conector.
 */
public class Conector {
	/**
	 * Variable url.
	 */
	private String url = "primeraBase.bd";
	/**
	 * Variable connect del tipo Connection.
	 */
	Connection connect;
	//ConexionHibernate connectionHibernate; //Atributo agregado
	
	//Constructor Agregado
	/*public Conector() {
		connectionHibernate = new ConexionHibernate(); 
	}*/
	
	/**
	 * Metodo connect.
	 */
	public void connect() {
		try {
			Servidor.log.append("Estableciendo conexión con la base de datos..." + System.lineSeparator());
			connect = DriverManager.getConnection("jdbc:sqlite:" + url);
			Servidor.log.append("Conexión con la base de datos establecida con éxito." + System.lineSeparator());
		} catch (SQLException ex) {
			Servidor.log.append("Fallo al intentar establecer la conexión con la base de datos. " + ex.getMessage()
					+ System.lineSeparator());
		}
	}

	/**
	 * Metodo close.
	 */
	public void close() {
		try {
			connect.close();
		} catch (SQLException ex) {
			Servidor.log.append("Error al intentar cerrar la conexión con la base de datos." + System.lineSeparator());
			Logger.getLogger(Conector.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Metodo registrarUsuario.
	 * 
	 * @param user
	 *            envia el usuario
	 * @return boolean
	 */
	public boolean registrarUsuario(final PaqueteUsuario user) {

		boolean usuarioRegistrado = false;
		boolean usuarioExiste = false;
		try {

			SessionFactory factory = new Configuration().configure().buildSessionFactory();
			Session session = factory.openSession();
			/*SessionFactory factory = connectionHibernate.getSessionFactory();
			Session session = factory.openSession();*/
			
			// Query para consultar si ya existe un usuario con ese nombre.
			CriteriaBuilder cb1 = session.getCriteriaBuilder();
			CriteriaQuery<Registro> criteriaQuery = cb1.createQuery(Registro.class);
			Root<Registro> customer = criteriaQuery.from(Registro.class);
			criteriaQuery.select(customer).where(cb1.equal(customer.get("usuario"), user.getUsername()));
			List<Registro> lista = session.createQuery(criteriaQuery).getResultList();
			/////////////////////////////////////////////////////////////////////// 777

			usuarioExiste = lista.iterator().hasNext();

			if (usuarioExiste) {
				Servidor.log.append(
						"El usuario " + user.getUsername() + " ya se encuentra en uso." + System.lineSeparator());
			} else {
				Registro r = new Registro();
				r.setIdPersonaje(user.getIdPj());
				r.setUsuario(user.getUsername());
				r.setPassword(user.getPassword());
				
				Transaction tx = session.beginTransaction();
				session.saveOrUpdate(r);
				tx.commit();
				
				usuarioRegistrado = true;

				Servidor.log.append("El usuario " + user.getUsername() + " se ha registrado." + System.lineSeparator());
			}
		} catch (HibernateException ex) {
			Servidor.log.append("Eror al intentar registrar el usuario " + user.getUsername() + System.lineSeparator());
			System.err.println(ex.getMessage());
			usuarioRegistrado = false;
		}
		return usuarioRegistrado && !usuarioExiste;
	}

	/**
	 * Metodo registrarPersonaje.
	 * 
	 * @param paquetePersonaje
	 *            manda el paquete del personaje
	 * @param paqueteUsuario
	 *            manda el paquete del usuario
	 * @return boolean
	 */
	public boolean registrarPersonaje(final PaquetePersonaje paquetePersonaje, final PaqueteUsuario paqueteUsuario) {
		boolean personajeRegistrado = false;
		boolean inventarioMochilaRegistrado = false;
		try {

			// Registro al personaje en la base de datos
			/*PreparedStatement stRegistrarPersonaje = connect.prepareStatement(
					"INSERT INTO personaje (idInventario, idMochila,casta,raza,fuerza,destreza,inteligencia,"
							+ "saludTope,energiaTope,nombre,experiencia,nivel,idAlianza) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
					PreparedStatement.RETURN_GENERATED_KEYS);
			stRegistrarPersonaje.setInt(1, -1);
			stRegistrarPersonaje.setInt(2, -1);
			stRegistrarPersonaje.setString(3, paquetePersonaje.getCasta());
			stRegistrarPersonaje.setString(4, paquetePersonaje.getRaza());
			stRegistrarPersonaje.setInt(5, paquetePersonaje.getFuerza());
			stRegistrarPersonaje.setInt(6, paquetePersonaje.getDestreza());
			stRegistrarPersonaje.setInt(7, paquetePersonaje.getInteligencia());
			stRegistrarPersonaje.setInt(8, paquetePersonaje.getSaludTope());
			stRegistrarPersonaje.setInt(9, paquetePersonaje.getEnergiaTope());
			stRegistrarPersonaje.setString(10, paquetePersonaje.getNombre());
			stRegistrarPersonaje.setInt(11, 0);
			stRegistrarPersonaje.setInt(12, 1);
			stRegistrarPersonaje.setInt(13, -1);
			stRegistrarPersonaje.execute();*/
			SessionFactory factory = new Configuration().configure().buildSessionFactory();
			Session session = factory.openSession();
			/*SessionFactory factory = connectionHibernate.getSessionFactory();
			Session session = factory.openSession();*/
			
			Personaje p = new Personaje();
			p.setIdInventario(-1);
			p.setIdMochila(-1);
			p.setCasta(paquetePersonaje.getCasta());
			p.setRaza(paquetePersonaje.getRaza());
			p.setFuerza(paquetePersonaje.getFuerza());
			p.setDestreza(paquetePersonaje.getDestreza());
			p.setInteligencia(paquetePersonaje.getInteligencia());
			p.setSaludTope(paquetePersonaje.getSaludTope());
			p.setEnergiaTope(paquetePersonaje.getEnergiaTope());
			p.setNombre(paquetePersonaje.getNombre());
			p.setExperiencia(0);
			p.setNivel(1);
			p.setIdAlianza(-1);
			
			session.getTransaction().begin();
			int idPersonaje = (Integer)session.save(p);
			session.getTransaction().commit();
	
			

			// Recupero la última key generada
			//ResultSet rs = stRegistrarPersonaje.getGeneratedKeys();

			personajeRegistrado = true;
			if (personajeRegistrado) {

				// Obtengo el id
				//int idPersonaje = rs.getInt(1);

				// Le asigno el id al paquete personaje que voy a devolver
				paquetePersonaje.setId(idPersonaje);

				// Le asigno el personaje al usuario
				PreparedStatement stAsignarPersonaje = connect
						.prepareStatement("UPDATE registro SET idPersonaje=? WHERE usuario=? AND password=?");
				stAsignarPersonaje.setInt(1, idPersonaje);
				stAsignarPersonaje.setString(2, paqueteUsuario.getUsername());
				stAsignarPersonaje.setString(3, paqueteUsuario.getPassword());
				stAsignarPersonaje.execute();

				// Por ultimo registro el inventario y la mochila
				inventarioMochilaRegistrado = this.registrarInventarioMochila(idPersonaje);
				if (inventarioMochilaRegistrado) {
					Servidor.log.append("El usuario " + paqueteUsuario.getUsername() + " ha creado el personaje "
							+ paquetePersonaje.getId() + System.lineSeparator());
				} else {
					Servidor.log.append(
							"Error al registrar la mochila y el inventario del usuario " + paqueteUsuario.getUsername()
									+ " con el personaje" + paquetePersonaje.getId() + System.lineSeparator());
				}
			}
		} catch (SQLException e) {
			Servidor.log.append(
					"Error al intentar crear el personaje " + paquetePersonaje.getNombre() + System.lineSeparator());
		}

		return personajeRegistrado && inventarioMochilaRegistrado;
	}

	/**
	 * Metodo registrarInventaioMochila.
	 * 
	 * @param idInventarioMochila
	 *            envia el id del inventario de la mochila
	 * @return boolean
	 */
	public boolean registrarInventarioMochila(final int idInventarioMochila) {
		boolean inventarioMochilaRegistrado = false;
		try {
			// Preparo la consulta para el registro el inventario en la base de
			// datos
			PreparedStatement stRegistrarInventario = connect
					.prepareStatement("INSERT INTO inventario(idInventario,manos1,"
							+ "manos2,pie,cabeza,pecho,accesorio) VALUES (?,-1,-1,-1,-1,-1,-1)");
			stRegistrarInventario.setInt(1, idInventarioMochila);

			// Preparo la consulta para el registro la mochila en la base de
			// datos
			PreparedStatement stRegistrarMochila = connect
					.prepareStatement("INSERT INTO mochila(idMochila,item1,item2,item3,item4,item5,"
							+ "item6,item7,item8,item9,item10,item11,item12,item13,item14,item15,"
							+ "item16,item17,item18,item19,item20) "
							+ "VALUES(?,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1)");
			stRegistrarMochila.setInt(1, idInventarioMochila);

			// Registro inventario y mochila
			stRegistrarInventario.execute();
			stRegistrarMochila.execute();

			// Le asigno el inventario y la mochila al personaje
			PreparedStatement stAsignarPersonaje = connect
					.prepareStatement("UPDATE personaje SET idInventario=?, idMochila=? WHERE idPersonaje=?");
			stAsignarPersonaje.setInt(1, idInventarioMochila);
			stAsignarPersonaje.setInt(2, idInventarioMochila);
			stAsignarPersonaje.setInt(3, idInventarioMochila);
			stAsignarPersonaje.execute();

			Servidor.log.append("Se ha registrado el inventario de " + idInventarioMochila + System.lineSeparator());
			inventarioMochilaRegistrado = true;

		} catch (SQLException e) {
			Servidor.log.append("Error al registrar el inventario de " + idInventarioMochila + System.lineSeparator());
		}

		return inventarioMochilaRegistrado;
	}

	/**
	 * Metodo loguearUsuario.
	 * 
	 * @param user
	 *            envia el usuario
	 * @return booelan
	 */
	public boolean loguearUsuario(final PaqueteUsuario user) {
		boolean existeInicioSesion = false;
		try {
			// Busco usuario y contraseña
			PreparedStatement st = connect
					.prepareStatement("SELECT * FROM registro WHERE usuario = ? AND password = ? ");
			st.setString(1, user.getUsername());
			st.setString(2, user.getPassword());
			ResultSet result = st.executeQuery();

			existeInicioSesion = result.next();

			if (existeInicioSesion) {
				Servidor.log
						.append("El usuario " + user.getUsername() + " ha iniciado sesión." + System.lineSeparator());
			} else {
				Servidor.log.append("El usuario " + user.getUsername()
						+ " ha realizado un intento fallido de inicio de sesión." + System.lineSeparator());
			}
		} catch (SQLException e) {
			Servidor.log
					.append("El usuario " + user.getUsername() + " fallo al iniciar sesión." + System.lineSeparator());
		}
		return existeInicioSesion;
	}

	/**
	 * metodo actualizarPersonaje.
	 * 
	 * @param paquetePersonaje
	 *            envia el paquete del personaje
	 */
	public void actualizarPersonaje(final PaquetePersonaje paquetePersonaje) {
		try {
			int i = 2;
			int j = 1;
			PreparedStatement stActualizarPersonaje = connect
					.prepareStatement("UPDATE personaje SET fuerza=?, destreza=?, inteligencia=?, saludTope=?,"
							+ " energiaTope=?, experiencia=?, nivel=? " + "  WHERE idPersonaje=?");

			stActualizarPersonaje.setInt(1, paquetePersonaje.getFuerza());
			stActualizarPersonaje.setInt(2, paquetePersonaje.getDestreza());
			stActualizarPersonaje.setInt(3, paquetePersonaje.getInteligencia());
			stActualizarPersonaje.setInt(4, paquetePersonaje.getSaludTope());
			stActualizarPersonaje.setInt(5, paquetePersonaje.getEnergiaTope());
			stActualizarPersonaje.setInt(6, paquetePersonaje.getExperiencia());
			stActualizarPersonaje.setInt(7, paquetePersonaje.getNivel());
			stActualizarPersonaje.setInt(8, paquetePersonaje.getId());
			stActualizarPersonaje.executeUpdate();

			PreparedStatement stDameItemsID = connect.prepareStatement("SELECT * FROM mochila WHERE idMochila = ?");
			stDameItemsID.setInt(1, paquetePersonaje.getId());
			ResultSet resultadoItemsID = stDameItemsID.executeQuery();
			PreparedStatement stDatosItem = connect.prepareStatement("SELECT * FROM item WHERE idItem = ?");
			ResultSet resultadoDatoItem = null;
			paquetePersonaje.eliminarItems();

			while (j <= 9) {
				if (resultadoItemsID.getInt(i) != -1) {
					stDatosItem.setInt(1, resultadoItemsID.getInt(i));
					resultadoDatoItem = stDatosItem.executeQuery();

					paquetePersonaje.anadirItem(resultadoDatoItem.getInt("idItem"),
							resultadoDatoItem.getString("nombre"), resultadoDatoItem.getInt("wereable"),
							resultadoDatoItem.getInt("bonusSalud"), resultadoDatoItem.getInt("bonusEnergia"),
							resultadoDatoItem.getInt("bonusFuerza"), resultadoDatoItem.getInt("bonusDestreza"),
							resultadoDatoItem.getInt("bonusInteligencia"), resultadoDatoItem.getString("foto"),
							resultadoDatoItem.getString("fotoEquipado"));
				}
				i++;
				j++;
			}
			Servidor.log.append("El personaje " + paquetePersonaje.getNombre() + " se ha actualizado con éxito."
					+ System.lineSeparator());
		} catch (SQLException e) {
			Servidor.log.append("Fallo al intentar actualizar el personaje " + paquetePersonaje.getNombre()
					+ System.lineSeparator());
		}

	}

	/**
	 * Getter del personaje.
	 * 
	 * @param user
	 *            envia el usuario
	 * @return personaje del tipo PaquetePersonaje
	 * @throws IOException
	 *             excepcion
	 */
	public PaquetePersonaje getPersonaje(final PaqueteUsuario user) throws IOException {
		PaquetePersonaje personaje = null;
		int i = 2;
		int j = 0;
		try {
			// Selecciono el personaje de ese usuario
			PreparedStatement st = connect.prepareStatement("SELECT * FROM registro WHERE usuario = ?");
			st.setString(1, user.getUsername());
			ResultSet result = st.executeQuery();

			// Obtengo el id
			int idPersonaje = result.getInt("idPersonaje");

			// Selecciono los datos del personaje
			PreparedStatement stSeleccionarPersonaje = connect
					.prepareStatement("SELECT * FROM personaje WHERE idPersonaje = ?");
			stSeleccionarPersonaje.setInt(1, idPersonaje);
			result = stSeleccionarPersonaje.executeQuery();
			// Traigo los id de los items correspondientes a mi personaje
			PreparedStatement stDameItemsID = connect.prepareStatement("SELECT * FROM mochila WHERE idMochila = ?");
			stDameItemsID.setInt(1, idPersonaje);
			ResultSet resultadoItemsID = stDameItemsID.executeQuery();
			// Traigo los datos del item
			PreparedStatement stDatosItem = connect.prepareStatement("SELECT * FROM item WHERE idItem = ?");

			// Obtengo los atributos del personaje
			personaje = new PaquetePersonaje();
			personaje.setId(idPersonaje);
			personaje.setRaza(result.getString("raza"));
			personaje.setCasta(result.getString("casta"));
			personaje.setFuerza(result.getInt("fuerza"));
			personaje.setInteligencia(result.getInt("inteligencia"));
			personaje.setDestreza(result.getInt("destreza"));
			personaje.setEnergiaTope(result.getInt("energiaTope"));
			personaje.setSaludTope(result.getInt("saludTope"));
			personaje.setNombre(result.getString("nombre"));
			personaje.setExperiencia(result.getInt("experiencia"));
			personaje.setNivel(result.getInt("nivel"));

			ResultSet resultadoDatoItem;

			while (j <= 9) {
				if (resultadoItemsID.getInt(i) != -1) {
					stDatosItem.setInt(1, resultadoItemsID.getInt(i));
					resultadoDatoItem = stDatosItem.executeQuery();
					personaje.anadirItem(resultadoDatoItem.getInt("idItem"), resultadoDatoItem.getString("nombre"),
							resultadoDatoItem.getInt("wereable"), resultadoDatoItem.getInt("bonusSalud"),
							resultadoDatoItem.getInt("bonusEnergia"), resultadoDatoItem.getInt("bonusFuerza"),
							resultadoDatoItem.getInt("bonusDestreza"), resultadoDatoItem.getInt("bonusInteligencia"),
							resultadoDatoItem.getString("foto"), resultadoDatoItem.getString("fotoEquipado"));
				}
				i++;
				j++;
			}

			// Devuelvo el paquete personaje con sus datos

		} catch (SQLException ex) {
			Servidor.log
					.append("Fallo al intentar recuperar el personaje " + user.getUsername() + System.lineSeparator());
			Servidor.log.append(ex.getMessage() + System.lineSeparator());
		}

		return personaje;
	}

	/**
	 * Metodo PaqueteUsuario.
	 * 
	 * @param usuario
	 *            envia el usuario
	 * @return paqueteUsuario
	 */
	public PaqueteUsuario getUsuario(final String usuario) {
		PaqueteUsuario paqueteUsuario = null;

		try {
			PreparedStatement st = connect.prepareStatement("SELECT * FROM registro WHERE usuario = ?");
			st.setString(1, usuario);
			ResultSet result = st.executeQuery();

			String password = result.getString("password");
			int idPersonaje = result.getInt("idPersonaje");

			paqueteUsuario = new PaqueteUsuario();
			paqueteUsuario.setUsername(usuario);
			paqueteUsuario.setPassword(password);
			paqueteUsuario.setIdPj(idPersonaje);

		} catch (SQLException e) {
			Servidor.log.append("Fallo al intentar recuperar el usuario " + usuario + System.lineSeparator());
			Servidor.log.append(e.getMessage() + System.lineSeparator());
		}

		return paqueteUsuario;
	}

	/**
	 * Metodo actualizarInventario.
	 * 
	 * @param paquetePersonaje
	 *            envia el paquete del personaje
	 */
	public void actualizarInventario(final PaquetePersonaje paquetePersonaje) {
		int i = 0;
		PreparedStatement stActualizarMochila;
		try {
			stActualizarMochila = connect.prepareStatement("UPDATE mochila SET item1=? ,item2=? ,"
					+ "item3=? ,item4=? ,item5=? ,item6=? ,item7=? ,item8=? ,item9=? "
					+ ",item10=? ,item11=? ,item12=? ,item13=? ,item14=? ,item15=? ,item16=? ,item17=? ,"
					+ "item18=? ,item19=? ,item20=? WHERE idMochila=?");
			while (i < paquetePersonaje.getCantItems()) {
				stActualizarMochila.setInt(i + 1, paquetePersonaje.getItemID(i));
				i++;
			}
			for (int j = paquetePersonaje.getCantItems(); j < 20; j++) {
				stActualizarMochila.setInt(j + 1, -1);
			}
			stActualizarMochila.setInt(21, paquetePersonaje.getId());
			stActualizarMochila.executeUpdate();

		} catch (SQLException e) {
		}
	}

	/**
	 * Metodo actualizarInventario.
	 * 
	 * @param idPersonaje
	 *            envia el idPersonaje
	 */
	public void actualizarInventario(final int idPersonaje) {
		int i = 0;
		PaquetePersonaje paquetePersonaje = Servidor.getPersonajesConectados().get(idPersonaje);
		PreparedStatement stActualizarMochila;
		try {
			stActualizarMochila = connect.prepareStatement("UPDATE mochila SET item1=? ,item2=? ,item3=? ,"
					+ "item4=? ,item5=? ,item6=? ,item7=? ,item8=? ,item9=? "
					+ ",item10=? ,item11=? ,item12=? ,item13=? ,item14=? ,item15=? ,item16=? ,item17=? ,"
					+ "item18=? ,item19=? ,item20=? WHERE idMochila=?");
			while (i < paquetePersonaje.getCantItems()) {
				stActualizarMochila.setInt(i + 1, paquetePersonaje.getItemID(i));
				i++;
			}
			if (paquetePersonaje.getCantItems() < 9) {
				int itemGanado = new Random().nextInt(29);
				itemGanado += 1;
				stActualizarMochila.setInt(paquetePersonaje.getCantItems() + 1, itemGanado);
				for (int j = paquetePersonaje.getCantItems() + 2; j < 20; j++) {
					stActualizarMochila.setInt(j, -1);
				}
			} else {
				for (int j = paquetePersonaje.getCantItems() + 1; j < 20; j++) {
					stActualizarMochila.setInt(j, -1);
				}
			}
			stActualizarMochila.setInt(21, paquetePersonaje.getId());
			stActualizarMochila.executeUpdate();

		} catch (SQLException e) {
			Servidor.log.append("Falló al intentar actualizar inventario de" + idPersonaje + "\n");
		}
	}

	/**
	 * Metodo actualizarPersonajeSubioNivel.
	 * 
	 * @param paquetePersonaje
	 *            envia el paquete del personaje
	 */
	public void actualizarPersonajeSubioNivel(final PaquetePersonaje paquetePersonaje) {
		try {
			PreparedStatement stActualizarPersonaje = connect
					.prepareStatement("UPDATE personaje SET fuerza=?, destreza=?, inteligencia=?,"
							+ " saludTope=?, energiaTope=?, experiencia=?, nivel=? " + "  WHERE idPersonaje=?");

			stActualizarPersonaje.setInt(1, paquetePersonaje.getFuerza());
			stActualizarPersonaje.setInt(2, paquetePersonaje.getDestreza());
			stActualizarPersonaje.setInt(3, paquetePersonaje.getInteligencia());
			stActualizarPersonaje.setInt(4, paquetePersonaje.getSaludTope());
			stActualizarPersonaje.setInt(5, paquetePersonaje.getEnergiaTope());
			stActualizarPersonaje.setInt(6, paquetePersonaje.getExperiencia());
			stActualizarPersonaje.setInt(7, paquetePersonaje.getNivel());
			stActualizarPersonaje.setInt(8, paquetePersonaje.getId());

			stActualizarPersonaje.executeUpdate();

			Servidor.log.append("El personaje " + paquetePersonaje.getNombre() + " se ha actualizado con éxito."
					+ System.lineSeparator());
		} catch (SQLException e) {
			Servidor.log.append("Fallo al intentar actualizar el personaje " + paquetePersonaje.getNombre()
					+ System.lineSeparator());
		}
	}
}
