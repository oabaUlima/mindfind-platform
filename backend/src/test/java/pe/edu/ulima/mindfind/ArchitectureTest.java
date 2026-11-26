package pe.edu.ulima.mindfind;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackages;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Architecture rules as executable tests. Enforces pure DDD layers
 * and strict isolation between {@code identidad_acreditacion}, {@code triage_matching} and {@code shared}.
 */
@AnalyzeClasses(packages = "pe.edu.ulima.mindfind", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    /** The domain is pure Java: no JPA/Hibernate, no Spring. */
    @ArchTest
    static final ArchRule domainIsPure = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "jakarta.persistence..",
                    "org.hibernate..",
                    "org.springframework..",
                    "jakarta.servlet..");

    /** The domain does not know the layers around it. */
    @ArchTest
    static final ArchRule domainDoesNotDependOnOuterLayers = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..application..",
                    "..infrastructure..",
                    "..interfaces..");

    /**
     * triage_matching may only use identidad_acreditacion's published contract:
     * the ACL facade and the integration events.
     */
    @ArchTest
    static final ArchRule triageMatchingOnlyUsesIdentidadPublishedContract = noClasses()
            .that().resideInAPackage("..triage_matching..")
            .should().dependOnClassesThat(
                    resideInAPackage("..identidad_acreditacion..")
                            .and(resideOutsideOfPackages(
                                    "..identidad_acreditacion.interfaces.acl..",
                                    "..identidad_acreditacion.interfaces.events..")));

    /**
     * identidad_acreditacion must not access internal layers of triage_matching
     * (upstream -> downstream isolation).
     */
    @ArchTest
    static final ArchRule identidadAcreditacionDoesNotDependOnTriageMatching = noClasses()
            .that().resideInAPackage("..identidad_acreditacion..")
            .should().dependOnClassesThat().resideInAPackage("..triage_matching..");

    /** Only infrastructure classes may touch JPA entities. */
    @ArchTest
    static final ArchRule persistenceModelStaysInInfrastructure = noClasses()
            .that().resideOutsideOfPackages("..infrastructure..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure.persistence.jpa.entities..");

    /** shared is purely technical and never depends on business bounded contexts. */
    @ArchTest
    static final ArchRule sharedDoesNotDependOnBoundedContexts = noClasses()
            .that().resideInAPackage("..shared..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..identidad_acreditacion..",
                    "..triage_matching..");
}
