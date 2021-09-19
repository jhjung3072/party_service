package com.party;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packagesOf = App.class)
public class PackageDependencyTests {

    private static final String STUDY = "..modules.study..";
    private static final String EVENT = "..modules.event..";
    private static final String ACCOUNT = "..modules.account..";
    private static final String TAG = "..modules.tag..";
    private static final String ZONE = "..modules.zone..";
    private static final String MAIN = "..modules.main..";

    @ArchTest
    ArchRule modulesPackageRule = classes().that().resideInAPackage("com.party.modules..")
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage("com.party.modules..");

    @ArchTest //study는 study, event, main 에 참조 되어진다.
    ArchRule studyPackageRule = classes().that().resideInAPackage(STUDY)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(STUDY, EVENT, MAIN);

    @ArchTest // event에 들어있는 것들은 event, study, account를 참조한다.
    ArchRule eventPackageRule = classes().that().resideInAPackage(EVENT)
            .should().accessClassesThat().resideInAnyPackage(STUDY, ACCOUNT, EVENT);

    @ArchTest // account에 들어있는 것들은 tag, zone, account를 참조한다.
    ArchRule accountPackageRule = classes().that().resideInAPackage(ACCOUNT)
            .should().accessClassesThat().resideInAnyPackage(TAG, ZONE, ACCOUNT);

    @ArchTest
    ArchRule cycleCheck = slices().matching("com.party.modules.(*)..")
            .should().beFreeOfCycles();
}
