package webserver;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoMySQL {
	public static String status = "Nao conectou...";
	private static Connection connection = null;

    public ConexaoMySQL() {

    }

    public static java.sql.Connection getConexao() {
    	 //atributo do tipo Connection     

        try {
            // Carregando o JDBC Driver padr�o
            String driverName = "com.mysql.jdbc.Driver";

            Class.forName(driverName);
            // Configurando a nossa conex�o com um banco de dados//

            String url = "jdbc:mysql://localhost/trabalho_comp_2";

            //Testa sua conex�o//
            if (connection == null) {
                status = ("STATUS--->Conectado com sucesso!");
                connection = DriverManager.getConnection(url, "root", "");
            }
            
            System.err.println(status);
            
            return connection;

        } catch (ClassNotFoundException e) {  //Driver n�o encontrado
            System.out.println("O driver expecificado nao foi encontrado.");
            return null;

        } catch (SQLException e) {

            //N�o conseguindo se conectar ao banco
            System.out.println("Nao foi possivel conectar ao Banco de Dados.");
            return null;
        }
    }

    //M�todo que retorna o status da sua conex�o//
    public static String statusConnection() {
        return status;
    }

    //M�todo que fecha sua conex�o//
    public static boolean FecharConexao() {
        try {
            ConexaoMySQL.getConexao().close();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    //M�todo que reinicia sua conex�o//
    public static java.sql.Connection ReiniciarConexao() {
        FecharConexao();
        return ConexaoMySQL.getConexao();
    }
}
