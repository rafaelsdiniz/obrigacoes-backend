package br.com.obrigacoes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "br.com.obrigacoes", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    @ArchTest
    static final ArchRule repositoriosSoSaoUsadosPorServicos = classes()
            .that().areAssignableTo(Repository.class)
            .should().onlyHaveDependentClassesThat().areAnnotatedWith(Service.class)
            .because("controllers e domínio não acessam o banco diretamente");

    @ArchTest
    static final ArchRule controllersNaoAcessamRepositorios = noClasses()
            .that().areAnnotatedWith(RestController.class)
            .should().dependOnClassesThat().areAssignableTo(Repository.class);

    @ArchTest
    static final ArchRule entidadesNaoConhecemAWebNemOsDtos = noClasses()
            .that().areAnnotatedWith(Entity.class)
            .should().dependOnClassesThat()
            .resideInAnyPackage("..dto..", "org.springframework.web..", "org.springframework.http..");

    @ArchTest
    static final ArchRule servicosNaoConhecemHttp = noClasses()
            .that().areAnnotatedWith(Service.class)
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework.http..", "org.springframework.web.bind..", "jakarta.servlet..");

    @ArchTest
    static final ArchRule compartilhadoNaoDependeDasFuncionalidades = noClasses()
            .that().resideInAPackage("br.com.obrigacoes.shared..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("br.com.obrigacoes.empresa..", "br.com.obrigacoes.usuario..",
                    "br.com.obrigacoes.obrigacao..", "br.com.obrigacoes.anexo..", "br.com.obrigacoes.config..");

    @ArchTest
    static final ArchRule modulosSemDependenciaCiclica = slices()
            .matching("br.com.obrigacoes.(*)..")
            .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule semInjecaoPorCampo = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

    @ArchTest
    static final ArchRule semSaidaPadrao = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

    @ArchTest
    static final ArchRule semJavaUtilLogging = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;
}
