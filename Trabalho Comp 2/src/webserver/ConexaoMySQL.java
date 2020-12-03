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
            // Carregando o JDBC Driver padrão
            String driverName = "com.mysql.jdbc.Driver";

            Class.forName(driverName);
            // Configurando a nossa conexão com um banco de dados//

            String url = "jdbc:mysql://localhost/trabalho_comp_2";

            //Testa sua conexão//
            if (connection == null) {
                status = ("STATUS--->Conectado com sucesso!");
                connection = DriverManager.getConnection(url, "root", "");
            }
            
            System.err.println(status);
            
            return connection;

        } catch (ClassNotFoundException e) {  //Driver não encontrado
            System.out.println("O driver expecificado nao foi encontrado.");
            return null;

        } catch (SQLException e) {

            //Não conseguindo se conectar ao banco
            System.out.println("Nao foi possivel conectar ao Banco de Dados.");
            return null;
        }
    }

    //Método que retorna o status da sua conexão//
    public static String statusConnection() {
        return status;
    }

    //Método que fecha sua conexão//
    public static boolean FecharConexao() {
        try {
            ConexaoMySQL.getConexao().close();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    //Método que reinicia sua conexão//
    public static java.sql.Connection ReiniciarConexao() {
        FecharConexao();
        return ConexaoMySQL.getConexao();
    }
}
