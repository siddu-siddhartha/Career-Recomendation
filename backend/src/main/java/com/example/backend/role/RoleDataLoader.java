package com.example.backend.role;

import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SuppressWarnings("unused")
public class RoleDataLoader {
    @Bean
    CommandLineRunner seedRoles(CareerRoleRepository roles) {
        return args -> {
            List<CareerRole> catalog = List.of(
                    role("Software Engineer", "Build reliable web and backend systems", "bachelor", "Technology", Set.of(skill("Java", 5), skill("SQL", 3), skill("Git", 2)), Set.of("building", "problem solving"), "technical", "analytical", "building"),
                    role("Data Analyst", "Turn business data into actionable insight", "bachelor", "Analytics", Set.of(skill("SQL", 5), skill("Python", 4), skill("Statistics", 3)), Set.of("analysis", "business"), "analytical", "technical", "business"),
                    role("Machine Learning Engineer", "Productionize predictive models", "master", "Artificial Intelligence", Set.of(skill("Python", 5), skill("Machine Learning", 5), skill("Cloud", 3)), Set.of("research", "experimentation"), "technical", "analytical", "science"),
                    role("UX Designer", "Design clear and useful digital experiences", "bachelor", "Design", Set.of(skill("User Research", 4), skill("Figma", 4), skill("Prototyping", 3)), Set.of("design", "empathy"), "creative", "people", "analytical"),
                    role("Product Manager", "Guide products from discovery to delivery", "bachelor", "Product", Set.of(skill("Communication", 4), skill("Product Strategy", 5), skill("Analytics", 3)), Set.of("leadership", "business"), "leadership", "business", "communication"),
                    role("Frontend Developer", "Create accessible interfaces for web products", "associate", "Technology", Set.of(skill("JavaScript", 5), skill("CSS", 4), skill("Accessibility", 3)), Set.of("building", "design"), "technical", "creative", "building"),
                    role("Full Stack Developer", "Build product features across browser and server", "bachelor", "Technology", Set.of(skill("JavaScript", 4), skill("APIs", 4), skill("SQL", 3)), Set.of("building", "problem solving"), "technical", "analytical", "building"),
                    role("Data Scientist", "Use statistics and code to answer complex questions", "master", "Analytics", Set.of(skill("Python", 5), skill("Statistics", 5), skill("Data Visualization", 3)), Set.of("research", "analysis"), "analytical", "technical", "science"),
                    role("Data Engineer", "Build the systems that move and prepare data", "bachelor", "Analytics", Set.of(skill("SQL", 5), skill("Python", 4), skill("Data Pipelines", 4)), Set.of("systems", "problem solving"), "technical", "analytical", "building"),
                    role("Cybersecurity Analyst", "Protect organizations from digital threats", "bachelor", "Cybersecurity", Set.of(skill("Networking", 4), skill("Security", 5), skill("Risk Analysis", 3)), Set.of("investigation", "protection"), "technical", "analytical"),
                    role("Cloud Engineer", "Design and operate scalable cloud infrastructure", "bachelor", "Technology", Set.of(skill("Cloud", 5), skill("Linux", 4), skill("Networking", 3)), Set.of("systems", "building"), "technical", "analytical", "building"),
                    role("DevOps Engineer", "Automate software delivery and reliable operations", "bachelor", "Technology", Set.of(skill("Linux", 4), skill("Cloud", 4), skill("Automation", 5)), Set.of("systems", "problem solving"), "technical", "analytical", "building"),
                    role("QA Automation Engineer", "Make software quality measurable and repeatable", "associate", "Technology", Set.of(skill("Testing", 5), skill("Automation", 4), skill("Programming", 3)), Set.of("quality", "problem solving"), "technical", "analytical", "building"),
                    role("Mobile App Developer", "Build useful experiences for phones and tablets", "associate", "Technology", Set.of(skill("Mobile Development", 5), skill("Programming", 4), skill("UX", 3)), Set.of("building", "design"), "technical", "creative", "building"),
                    role("Database Administrator", "Keep business data secure, available, and organized", "associate", "Technology", Set.of(skill("SQL", 5), skill("Data Modeling", 4), skill("Security", 3)), Set.of("systems", "organization"), "technical", "analytical"),
                    role("UX Researcher", "Understand people and turn evidence into product direction", "bachelor", "Design", Set.of(skill("User Research", 5), skill("Interviewing", 4), skill("Synthesis", 4)), Set.of("research", "empathy"), "creative", "people", "analytical"),
                    role("Product Designer", "Shape product flows, prototypes, and visual systems", "bachelor", "Design", Set.of(skill("Figma", 5), skill("Prototyping", 4), skill("User Research", 3)), Set.of("design", "building"), "creative", "people", "building"),
                    role("Graphic Designer", "Create visual identities and communication materials", "associate", "Design", Set.of(skill("Typography", 4), skill("Illustration", 3), skill("Adobe Creative Suite", 5)), Set.of("visual design", "storytelling"), "creative"),
                    role("Content Designer", "Make complex product information clear and useful", "bachelor", "Design", Set.of(skill("Writing", 5), skill("Content Strategy", 4), skill("UX", 3)), Set.of("writing", "empathy"), "creative", "communication", "people"),
                    role("Project Manager", "Coordinate people, scope, and delivery across teams", "bachelor", "Operations", Set.of(skill("Planning", 5), skill("Communication", 4), skill("Risk Management", 3)), Set.of("organization", "leadership"), "leadership", "communication", "business"),
                    role("Business Analyst", "Translate business needs into clear decisions and requirements", "bachelor", "Business", Set.of(skill("Requirements Analysis", 5), skill("SQL", 3), skill("Communication", 4)), Set.of("analysis", "business"), "analytical", "business", "communication"),
                    role("Digital Marketing Specialist", "Grow audiences through useful campaigns and content", "bachelor", "Marketing", Set.of(skill("Content Strategy", 4), skill("Analytics", 4), skill("SEO", 5)), Set.of("storytelling", "business"), "creative", "business", "analytical"),
                    role("Financial Analyst", "Help teams make better decisions with financial models", "bachelor", "Finance", Set.of(skill("Financial Modeling", 5), skill("Excel", 4), skill("Forecasting", 4)), Set.of("analysis", "business"), "analytical", "business"),
                    role("Accountant", "Prepare accurate financial records and reporting", "bachelor", "Finance", Set.of(skill("Accounting", 5), skill("Excel", 4), skill("Attention to Detail", 4)), Set.of("accuracy", "organization"), "analytical", "business"),
                    role("Human Resources Specialist", "Help people and organizations work well together", "bachelor", "People Operations", Set.of(skill("Communication", 5), skill("Recruiting", 4), skill("Conflict Resolution", 3)), Set.of("people", "support"), "people", "communication", "leadership"),
                    role("Teacher", "Help learners build knowledge, confidence, and skills", "bachelor", "Education", Set.of(skill("Communication", 5), skill("Lesson Planning", 4), skill("Mentoring", 4)), Set.of("learning", "community"), "people", "communication", "creative"),
                    role("Registered Nurse", "Provide evidence-based care and support for patients", "bachelor", "Healthcare", Set.of(skill("Patient Care", 5), skill("Communication", 4), skill("Clinical Assessment", 4)), Set.of("care", "community"), "people", "science", "hands-on"),
                    role("Public Health Analyst", "Use research and data to improve community health", "bachelor", "Healthcare", Set.of(skill("Statistics", 4), skill("Research", 5), skill("Communication", 3)), Set.of("health", "community"), "people", "analytical", "science"),
                    role("Environmental Scientist", "Study environmental systems and support sustainable choices", "bachelor", "Environment", Set.of(skill("Research", 5), skill("Data Analysis", 4), skill("GIS", 3)), Set.of("environment", "science"), "science", "hands-on", "analytical"),
                    role("Mechanical Engineer", "Design and improve machines, products, and processes", "bachelor", "Engineering", Set.of(skill("CAD", 5), skill("Physics", 4), skill("Prototyping", 3)), Set.of("design", "problem solving"), "technical", "hands-on", "analytical"),
                    role("Electrical Engineer", "Develop and test electrical and electronic systems", "bachelor", "Engineering", Set.of(skill("Circuit Design", 5), skill("Physics", 4), skill("Testing", 3)), Set.of("systems", "problem solving"), "technical", "hands-on", "analytical"),
                    role("Civil Engineer", "Plan and deliver infrastructure for communities", "bachelor", "Engineering", Set.of(skill("Structural Design", 5), skill("CAD", 4), skill("Project Planning", 3)), Set.of("building", "community"), "technical", "hands-on", "building"),
                    role("Entrepreneur", "Turn an opportunity into a sustainable product or service", "associate", "Business", Set.of(skill("Strategy", 4), skill("Sales", 4), skill("Budgeting", 3)), Set.of("independence", "innovation"), "leadership", "business", "creative"),
                    role("Technical Writer", "Explain technical products through clear documentation", "bachelor", "Technology", Set.of(skill("Writing", 5), skill("Technical Concepts", 4), skill("Information Design", 3)), Set.of("learning", "clarity"), "communication", "technical", "creative"),
                    role("Sales Engineer", "Help customers choose technical solutions that fit their needs", "bachelor", "Technology", Set.of(skill("Communication", 5), skill("Product Knowledge", 4), skill("Sales", 4)), Set.of("problem solving", "relationships"), "communication", "business", "technical"),
                    role("Operations Analyst", "Improve how services and teams work day to day", "bachelor", "Operations", Set.of(skill("Data Analysis", 4), skill("Process Improvement", 5), skill("Excel", 3)), Set.of("efficiency", "analysis"), "analytical", "leadership", "business"),
                    role("Supply Chain Analyst", "Plan the movement of goods, materials, and information", "bachelor", "Operations", Set.of(skill("Forecasting", 4), skill("Excel", 4), skill("Logistics", 5)), Set.of("planning", "analysis"), "analytical", "business", "leadership"),
                    role("Industrial Designer", "Develop useful, manufacturable products and objects", "bachelor", "Design", Set.of(skill("CAD", 4), skill("Prototyping", 5), skill("Visual Design", 4)), Set.of("making", "design"), "creative", "hands-on", "building"),
                    role("Social Worker", "Connect individuals and families with practical support", "bachelor", "Community Services", Set.of(skill("Communication", 5), skill("Case Management", 4), skill("Advocacy", 4)), Set.of("care", "community"), "people", "communication", "helping"),
                    role("Video Editor", "Shape footage into clear, engaging visual stories", "associate", "Media", Set.of(skill("Video Editing", 5), skill("Storytelling", 4), skill("Audio", 3)), Set.of("storytelling", "media"), "creative", "hands-on", "communication")
            );
            for (CareerRole catalogRole : catalog) {
                if (!roles.existsByNameIgnoreCase(catalogRole.getName())) {
                    roles.save(catalogRole);
                }
            }
        };
    }

        private CareerRole role(String name, String description, String education, String industry,
                                                        Set<RequiredSkill> skills, Set<String> interests, String... assessmentCategories) {
                CareerRole role = new CareerRole(name, description, education, industry, skills, interests);
                role.setAssessmentCategories(Set.of(assessmentCategories));
                return role;
    }

    private RequiredSkill skill(String name, int weight) { return new RequiredSkill(name, weight); }
}