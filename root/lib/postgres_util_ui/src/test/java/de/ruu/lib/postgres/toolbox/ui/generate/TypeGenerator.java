package de.ruu.lib.postgres.toolbox.ui.generate;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.fx.comp.GeneratorFXCompBundle;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class TypeGenerator
{
	public static void main(String[] args) throws IOException, GeneratorException
	{
		generateJavaFXComponentPostgresUtilUI();
	}

	private static void generateJavaFXComponentPostgresUtilUI() throws GeneratorException, IOException
	{
		log.debug("creating java fx component bundle for postgres util ui component of r-uu library");
		GeneratorFXCompBundle postgresUtilUIComponentBundleGenerator =
				new GeneratorFXCompBundle
				(
						"de.ruu.lib.postgres.util.ui.generate",
						"PostgresUtilUI"
				);

		postgresUtilUIComponentBundleGenerator.run();
		log.debug("created  java fx component bundle for postgres util ui component of r-uu library");
	}
}
