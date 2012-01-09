package isolation;

public class Main {

	/**
	 * @param args[0] : full path to the repository directory
	 * @param args[1] : starting commit id
	 */
	public static void main(String[] args) 
	{
		Repository repo = RepositoryBuilder.buildRepository(args[0], args[1]);
	}

}
