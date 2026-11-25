package co.styleme.arquitectura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Pruebas que vigilan las reglas de la arquitectura hexagonal de StyleMe.
 *
 * ArchUnit lee las clases compiladas del proyecto y revisa de quién depende cada una.
 * Si alguien rompe una regla (por ejemplo, usa Spring dentro del dominio), esta prueba
 * falla y el proyecto no pasa "mvn test".
 *
 * Reglas: docs/planeacion/16-arquitectura-hexagonal.md (sección 2).
 */
@AnalyzeClasses(packages = "co.styleme", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaHexagonalTest {

    private static final String DOMINIO = "co.styleme.dominio..";
    private static final String APLICACION = "co.styleme.aplicacion..";
    private static final String INFRAESTRUCTURA = "co.styleme.infraestructura..";

    /** Los package-info.java solo documentan cada carpeta: no son clases del proyecto. */
    private static final String PACKAGE_INFO = "package-info";

    /**
     * R1 + R7 · Arquitectura de cebolla (hexagonal):
     * las dependencias apuntan hacia el centro y los adaptadores no se hablan entre sí.
     *
     *   adaptadores  →  aplicación  →  dominio
     */
    @ArchTest
    static final ArchRule R1_R7_dependencias_hacia_el_centro = onionArchitecture()
            // Las excepciones son parte del modelo: los modelos las lanzan al validar sus reglas
            .domainModels("co.styleme.dominio.modelo..", "co.styleme.dominio.excepcion..")
            .domainServices("co.styleme.dominio.servicio..")
            .applicationServices(APLICACION)
            .adapter("entrada", "co.styleme.infraestructura.adaptador.entrada..")
            .adapter("salida", "co.styleme.infraestructura.adaptador.salida..")
            // ConfiguracionBeans "enchufa" los casos de uso con los puertos (composition root):
            // pertenece a la infraestructura y, como los adaptadores, puede ver la aplicación
            .adapter("configuracion", "co.styleme.infraestructura.configuracion..")
            .withOptionalLayers(true);

    /** R2 · El dominio es Java puro: sin Spring, JPA, Jackson ni librerías de IA. */
    @ArchTest
    static final ArchRule R2_dominio_sin_frameworks = noClasses()
            .that().resideInAPackage(DOMINIO)
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "org.hibernate..",
                    "com.fasterxml.jackson..", "tools.jackson..", "com.google..")
            .because("el dominio no puede depender de frameworks (regla R2)")
            .allowEmptyShould(true);

    /** R3 · La aplicación (puertos y casos de uso) tampoco depende del framework. */
    @ArchTest
    static final ArchRule R3_aplicacion_sin_frameworks = noClasses()
            .that().resideInAPackage(APLICACION)
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "org.hibernate..")
            .because("los casos de uso se registran como beans en infraestructura/configuracion (regla R3)")
            .allowEmptyShould(true);

    /** R1 · Nada del centro puede conocer la infraestructura. */
    @ArchTest
    static final ArchRule R1_centro_no_conoce_infraestructura = noClasses()
            .that().resideInAnyPackage(DOMINIO, APLICACION)
            .should().dependOnClassesThat().resideInAPackage(INFRAESTRUCTURA)
            .allowEmptyShould(true);

    /** R4 · Los puertos de entrada son interfaces y terminan en "CasoUso". */
    @ArchTest
    static final ArchRule R4_puertos_de_entrada = classes()
            .that().resideInAPackage("co.styleme.aplicacion.puerto.entrada..")
            .and().doNotHaveSimpleName(PACKAGE_INFO)
            .and().areTopLevelClasses() // no las clases que Java genera solo (por ejemplo, para un switch)
            .should().beInterfaces()
            .andShould().haveSimpleNameEndingWith("CasoUso")
            .allowEmptyShould(true);

    /** R5 · Los puertos de salida son interfaces y terminan en "Puerto". */
    @ArchTest
    static final ArchRule R5_puertos_de_salida = classes()
            .that().resideInAPackage("co.styleme.aplicacion.puerto.salida..")
            .and().doNotHaveSimpleName(PACKAGE_INFO)
            .and().areTopLevelClasses() // no las clases que Java genera solo (por ejemplo, para un switch)
            .should().beInterfaces()
            .andShould().haveSimpleNameEndingWith("Puerto")
            .allowEmptyShould(true);

    /** Los casos de uso son clases (no interfaces) y terminan en "Servicio". */
    @ArchTest
    static final ArchRule casos_de_uso = classes()
            .that().resideInAPackage("co.styleme.aplicacion.casodeuso..")
            .and().doNotHaveSimpleName(PACKAGE_INFO)
            .and().areTopLevelClasses() // no las clases que Java genera solo (por ejemplo, para un switch)
            .should().notBeInterfaces()
            .andShould().haveSimpleNameEndingWith("Servicio")
            .allowEmptyShould(true);

    /** R7 · Los controladores REST solo llaman a casos de uso, nunca a repositorios ni adaptadores de salida. */
    @ArchTest
    static final ArchRule R7_controladores_solo_usan_casos_de_uso = noClasses()
            .that().resideInAPackage("co.styleme.infraestructura.adaptador.entrada..")
            .should().dependOnClassesThat().resideInAPackage("co.styleme.infraestructura.adaptador.salida..")
            .allowEmptyShould(true);
}
