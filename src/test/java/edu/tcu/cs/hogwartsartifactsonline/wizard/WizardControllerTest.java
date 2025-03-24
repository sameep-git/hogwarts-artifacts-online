package edu.tcu.cs.hogwartsartifactsonline.wizard;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tcu.cs.hogwartsartifactsonline.system.StatusCode;
import edu.tcu.cs.hogwartsartifactsonline.wizard.dto.WizardDto;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class WizardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    WizardService wizardService;

    List<Wizard> wizards;

    @BeforeEach
    void setUp() {
        this.wizards = new ArrayList<>();

        Wizard w1 = new Wizard();
        w1.setId(1);
        w1.setName("Albus Dumbledore");
        this.wizards.add(w1);

        Wizard w2 = new Wizard();
        w2.setId(2);
        w2.setName("Harry Potter");
        this.wizards.add(w2);

        Wizard w3 = new Wizard();
        w3.setId(3);
        w3.setName("Neville Longbottom");
        this.wizards.add(w3);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testFindWizardByIdSuccess() throws Exception {
        // Given
        given(this.wizardService.findById(1))
                .willReturn(this.wizards.get(0));

        // When and Then
        this.mockMvc.perform(get("/api/v1/wizards/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Albus Dumbledore"));
    }

    @Test
    void testFindWizardByIdNotFound() throws Exception {
        // Given
        given(this.wizardService.findById(1))
                .willThrow(new WizardNotFoundException(1));

        // When and Then
        this.mockMvc.perform(get("/api/v1/wizards/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find wizard with Id 1 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testFindAllSuccess() throws Exception {
        // Given
        given(this.wizardService.findAll()).willReturn(this.wizards);

        // When and Then
        this.mockMvc.perform(get("/api/v1/wizards").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(this.wizards.size())))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Albus Dumbledore"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].name").value("Harry Potter"));
    }

    @Test
    void testAddWizardSuccess() throws Exception {
        WizardDto wizardDto = new WizardDto(null, "Hermione Granger", 0);

        String json = this.objectMapper.writeValueAsString(wizardDto);

        Wizard savedWizard = new Wizard();
        savedWizard.setId(4);
        savedWizard.setName("Hermione Granger");

        // Given
        given(this.wizardService.save(Mockito.any(Wizard.class))).willReturn(savedWizard);

        // When and then
        this.mockMvc.perform(post("/api/v1/wizards").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.name").value("Hermione Granger"));
    }

    @Test
    void testUpdateWizardSuccess() throws Exception {
        WizardDto wizardDto = new WizardDto(null, "Updated wizard name", 0);

        Wizard updatedWizard = new Wizard();
        updatedWizard.setId(1);
        updatedWizard.setName("Updated wizard name");

        String json = this.objectMapper.writeValueAsString(updatedWizard);

        // Given. Arrange inputs and targets. Define the behavior of Mock object wizardService.
        given(this.wizardService.update(eq(1), Mockito.any(Wizard.class))).willReturn(updatedWizard);

        // When and then
        this.mockMvc.perform(put("/api/v1/wizards/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Updated wizard name"));
    }

    @Test
    void testUpdateWizardErrorWithNonExistentId() throws Exception {
        // Given. Arrange inputs and targets. Define the behavior of Mock object wizardService.
        given(this.wizardService.update(eq(5), Mockito.any(Wizard.class))).willThrow(new WizardNotFoundException(5));

        WizardDto wizardDto = new WizardDto(5, // This id does not exist in the database.
                "Updated wizard name",
                0);

        String json = this.objectMapper.writeValueAsString(wizardDto);

        // When and then
        this.mockMvc.perform(put("/api/v1/wizards/5").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find wizard with Id 5 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteWizardSuccess() throws Exception {
        // Given. Arrange inputs and targets. Define the behavior of Mock object wizardService.
        doNothing().when(this.wizardService).delete(3);

        // When and then
        this.mockMvc.perform(delete("/api/v1/wizards/3").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete Success"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteWizardErrorWithNonExistentId() throws Exception {
        // Given. Arrange inputs and targets. Define the behavior of Mock object wizardService.
        doThrow(new WizardNotFoundException(5)).when(this.wizardService).delete(5);

        // When and then
        this.mockMvc.perform(delete("/api/v1/wizards/5").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find wizard with Id 5 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
