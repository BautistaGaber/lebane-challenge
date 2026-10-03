package com.lebane.backend.seed;

import com.lebane.backend.department.entity.CurrencyCode;
import com.lebane.backend.department.entity.Department;
import com.lebane.backend.department.repository.DepartmentRepository;
import com.lebane.backend.image.entity.Image;
import com.lebane.backend.inquiry.entity.Inquiry;
import com.lebane.backend.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Component
@EnableConfigurationProperties(SeedProperties.class)
public class DatabaseSeeder implements ApplicationRunner {

    private static final long RANDOM_SEED = 20261001L;

    private final DepartmentRepository departmentRepository;
    private final StorageService storageService;
    private final SeedImageGenerator imageGenerator;
    private final SeedProperties properties;

    public DatabaseSeeder(DepartmentRepository departmentRepository, StorageService storageService, SeedImageGenerator imageGenerator, SeedProperties properties) {
        this.departmentRepository = departmentRepository;
        this.storageService = storageService;
        this.imageGenerator = imageGenerator;
        this.properties = properties;
    }


    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        if (!properties.enabled()) {
            log.info("Database seed is disabled");
            return;
        }

        seedMissingDepartments();

        repairMissingSeedImages();
    }

    private void seedMissingDepartments() {

        long existingDepartments = departmentRepository.count();

        if (existingDepartments >= properties.departmentCount()) {
            log.info("Database already contains {} departments. No departments need to be created.", existingDepartments);
            return;
        }

        int missingDepartments = properties.departmentCount() - (int) existingDepartments;

        log.info("Starting database seed. Creating {} departments.", missingDepartments);

        Random random = new Random(RANDOM_SEED + existingDepartments);

        for (int index = 0; index < missingDepartments; index++) {

            Department department = createDepartment(random, existingDepartments + index + 1);

            departmentRepository.saveAndFlush(department);

            createImages(department, random);

            createInquiries(department, random);
        }

        log.info("Database seed completed. Total departments: {}", departmentRepository.count());
    }

    private void repairMissingSeedImages() {

        log.info("Checking seed images in storage.");

        int checkedImages = 0;
        int repairedImages = 0;

        for (Department department : departmentRepository.findAll()) {

            for (Image image : department.getImages()) {

                String objectKey = image.getObjectKey();

                if (!objectKey.contains("/seed-")) {
                    continue;
                }

                checkedImages++;

                if (storageService.exists(objectKey)) {
                    continue;
                }

                log.warn("Missing seed image detected in storage: {}", objectKey);

                byte[] content = imageGenerator.generate(department.getTitle(), image.getDisplayOrder());

                storageService.upload(content, image.getContentType(), objectKey);

                repairedImages++;
            }
        }

        log.info("Seed image verification completed. Checked: {}, repaired: {}.", checkedImages, repairedImages);
    }

    private Department createDepartment(Random random,long sequence) {

        String[] neighborhoods = {
                "Palermo",
                "Belgrano",
                "Recoleta",
                "Caballito",
                "Almagro",
                "Villa Urquiza",
                "Núñez",
                "Colegiales",
                "San Telmo",
                "Villa Crespo"
        };

        String[] streets = {
                "Av. Santa Fe",
                "Av. Córdoba",
                "Av. Cabildo",
                "Av. Rivadavia",
                "Av. Corrientes",
                "Av. Juan B. Justo",
                "Av. Scalabrini Ortiz",
                "Av. Libertador"
        };

        String neighborhood =neighborhoods[random.nextInt(neighborhoods.length)];

        String street = streets[random.nextInt(streets.length)];

        CurrencyCode currency = random.nextBoolean() ? CurrencyCode.USD : CurrencyCode.ARS;

        BigDecimal price;

        if (currency == CurrencyCode.USD) {
            price = randomDecimal(random,60000,400000);
        } else {
            price = randomDecimal(random,50_000_000,500_000_000);
        }

        Department department = Department.create();

        department.setTitle("Departamento en " + neighborhood + " #" + sequence);

        department.setDescription("Propiedad generada para datos de prueba en " + neighborhood + ". Cuenta con espacios luminosos y distribución funcional.");

        department.setPrice(price);
        department.setCurrency(currency);

        department.setSquareMeters(randomDecimal(random,25,220));

        department.setAddress(street + " " + (500 + random.nextInt(9000)) + ", " + neighborhood);

        department.setLatitude(BigDecimal.valueOf(-34.70 + random.nextDouble() * 0.20).setScale(6,RoundingMode.HALF_UP));

        department.setLongitude(BigDecimal.valueOf(-58.55 + random.nextDouble() * 0.25).setScale(6,RoundingMode.HALF_UP));

        department.setAvailable(random.nextDouble() < 0.75);

        return department;
    }

    private void createImages(Department department,Random random) {

        int imageCount = random.nextInt(9);

        for (int index = 0;index < imageCount;index++) {

            byte[] content = imageGenerator.generate(department.getTitle(),index);

            String objectKey = "departments/" + department.getId() + "/seed-" + UUID.randomUUID() + ".png";

            storageService.upload(content,"image/png",objectKey);

            Image image = Image.create();

            image.setObjectKey(objectKey);
            image.setContentType("image/png");
            image.setDisplayOrder(index);
            image.setPrimaryImage(index == 0);

            department.addImage(image);
        }
    }

    private void createInquiries(Department department,Random random) {

        int inquiryCount = random.nextInt(41);

        for (int index = 0;index < inquiryCount;index++) {

            Inquiry inquiry = Inquiry.create();

            inquiry.setName("Cliente " + (index + 1));

            inquiry.setEmail("cliente" + department.getId() + "-" + index + "@example.com");

            inquiry.setMessage("Quisiera recibir más información sobre esta propiedad.");

            department.addInquiry(inquiry);
        }
    }

    private BigDecimal randomDecimal(Random random,double minimum,double maximum) {

        double value = minimum + (maximum - minimum) * random.nextDouble();

        return BigDecimal.valueOf(value).setScale(2,RoundingMode.HALF_UP);
    }
}