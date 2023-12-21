package com.example.capstone3.Service;

import com.example.capstone3.Api.ApiException;
import com.example.capstone3.DTO.RequestDTO;
import com.example.capstone3.Model.Member;
import com.example.capstone3.Model.Request;
import com.example.capstone3.Model.Team;
import com.example.capstone3.Repository.MemberRepository;
import com.example.capstone3.Repository.RequestRepository;
import com.example.capstone3.Repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final TeamService teamService;

    public List<Request> getAllRequests() {
        return requestRepository.findAll();
    }

    //تاكدي ان الليدر هو نفسه ليدر هذا الفريق
    //تاكدي ان الميمبر مب في تيم بعد
    public void addRequest(RequestDTO requestDTO) {
        Member member = memberRepository.findMemberById(requestDTO.getMember_id());
        Team team = teamRepository.findTeamById(requestDTO.getTeam_id());
        Request request1 = requestRepository.findRequestByMemberAndTeamId(member.getId(), team.getId()); //عشان مايرسل ركوست مرتين لنفس التيم
        if (member == null || team == null) {
            throw new ApiException("member or team is null, request cannot be created");
        }
        if (member.getIsBlacklist()) {
            throw new ApiException("sorry you are blocked");
        } //كلهم الليدر و الميمبر ولا وواحد بلاك لست
        if (member.getRole().equals("Leader")) {
            throw new ApiException("you must a be member to join not leader, change your role!");
        }
        if (member.getTeam() != null) {
            throw new ApiException("You Are Already In Team!");
        }
        if (request1 != null) {
            throw new ApiException("You Already Sent Request For This Team!");
        }
        if (team.getMaxCap() == 0) {
            throw new ApiException("you cant join to this team,capacity full");
        }
        if (member.getAge() < team.getHackathon().getMinAge() || member.getAge() > team.getHackathon().getMaxAge()) {
            throw new ApiException("You cant register in this hackathon because your age");
        }
        Request request = new Request(null, "Pending", member, team);
        requestRepository.save(request);
    }

    public void deleteRequest(Integer id) {
        Request request = requestRepository.findRequestById(id);
        if (request == null) {
            throw new ApiException("Request Not Found");
        }
        requestRepository.delete(request);
    }

    //1
    public ArrayList<ArrayList<String>> viewTeamRequestsForOneTeam(Integer leader_id, Integer team_id) {
        Member leader = memberRepository.findMemberById(leader_id);
        Team team = teamRepository.findTeamById(team_id);
        if (leader == null && team == null) {
            throw new ApiException("leader or team not found");
        }
        if (!leader.getRole().equals("Leader")) {
            throw new ApiException("You not leader");
        }
        for (Member m : team.getMembers()) {
            if (m.getId() == leader_id) {
                ArrayList<ArrayList<String>> requests = new ArrayList<>();
                for (Request request : team.getRequest()) {
                    ArrayList<String> memInfo = new ArrayList<>();
                    memInfo.add("request id :" + request.getId());
                    memInfo.add("name :" + request.getMember().getName());
                    memInfo.add("email :" + request.getMember().getEmail());
                    memInfo.add("age :" + request.getMember().getAge().toString());
                    memInfo.add("field :" + request.getMember().getField());
                    memInfo.add("skills :" + request.getMember().getSkills().toString());
                    memInfo.add("experience :" + request.getMember().getExperience().toString());
                    memInfo.add("nationality :" + request.getMember().getNationality());
                    memInfo.add("participation times :" + request.getMember().getParticipationTimes().toString());
                    memInfo.add("winning times :" + request.getMember().getWinningTimes().toString());
                    memInfo.add("reviews :" + request.getMember().getReviews().toString()); //////
                    requests.add(memInfo);
                }
                return requests;
            }
        }
        throw new ApiException("You are not leader for this team!");
    }

    //2
    public void acceptRequest(Integer leader_id, Integer request_id) {
        Member leader = memberRepository.findMemberById(leader_id);
        Request request = requestRepository.findRequestById(request_id);
        if (leader == null || request == null) {
            throw new ApiException("leader of request no found");
        }
        for (Member m : request.getTeam().getMembers()) {
            if (m.getId() == leader_id) {
                teamService.assignMemberToTeam(request.getTeam().getId(), request.getMember().getId());
                request.setStatus("Accepted");
                requestRepository.save(request);
            }
        }
        throw new ApiException("You are not leader for this team!");
    }

    //3
    public void rejectRequest(Integer leader_id, Integer request_id) {
        Member leader = memberRepository.findMemberById(leader_id);
        Request request = requestRepository.findRequestById(request_id);
        if (leader == null || request == null) {
            throw new ApiException("leader of request no found");
        }
        for (Member m : request.getTeam().getMembers()) {
            if (m.getId() == leader_id) {
                request.setStatus("Rejected");
                requestRepository.save(request);
            }
        }
        throw new ApiException("You are not leader for this team!");
    }

    //4
    //تاكدي ان الليدر هو نفسه ليدر هذا الفريق
    //تاكدي ان الميمبر مب في تيم بعد
    public void addRequestToMember(RequestDTO requestDTO,Integer leader_id) {
        Member leader = memberRepository.findMemberById(leader_id);
        Member member = memberRepository.findMemberById(requestDTO.getMember_id());
        Team team = teamRepository.findTeamById(requestDTO.getTeam_id());
        Request request1 = requestRepository.findRequestByMemberAndTeamId(member.getId(), team.getId()); //عشان مايرسل ركوست مرتين لنفس التيم
        if (member == null || team == null || leader == null) {
            throw new ApiException("member or leader or team is null, request cannot be created");
        }
        if (leader.getIsBlacklist()) {
            throw new ApiException("sorry you are blocked");
        }
        if (member.getIsBlacklist()) {
            throw new ApiException("sorry this member cannot join to any team ,he is blocked");
        }
        if (!leader.getRole().equals("Leader")) {
            throw new ApiException("you not a leader");
        }
        if (member.getRole().equals("Leader")) {
            throw new ApiException("member is leader");
        }
        if (member.getTeam() != null) {
            throw new ApiException("Member Already In Team!");
        }
        if (request1 != null) {
            throw new ApiException("You Already Sent Request For This Member!");
        }
        if (team.getMaxCap() == 0) {
            throw new ApiException("you cant add member with your team,capacity full");
        }
        if (member.getAge() < team.getHackathon().getMinAge() || member.getAge() > team.getHackathon().getMaxAge()) {
            throw new ApiException("member cant register in this hackathon because his age");
        }
        for (Member m : team.getMembers()) {
            if (m.getId() == leader.getId()) {
                Request request = new Request(null, "Pending", member, team);
                requestRepository.save(request);
            }
        }
    }

    //5
    public ArrayList<ArrayList<String>> viewRequestsForOneMember(Integer member_id) {
        Member member = memberRepository.findMemberById(member_id);
        if (member == null) {
            throw new ApiException("member not found");
        }
        ArrayList<ArrayList<String>> requests = new ArrayList<>();
        for (Request request : member.getRequests()) {
            ArrayList<String> teamInfo = new ArrayList<>();
            teamInfo.add("request id :" + request.getId());
            teamInfo.add("name :" + request.getTeam().getName());
            teamInfo.add("hackathon :" + request.getTeam().getHackathon());
            teamInfo.add("description :" + request.getTeam().getDescription());
            requests.add(teamInfo);
        }
        return requests;
    }

    //6
    public void memberAcceptRequest(Integer member_id, Integer request_id) {
        Request request = requestRepository.findRequestByMemberIdAndRequestId(member_id, request_id);
        if (request == null) {
            throw new ApiException("request not found");
        }

        teamService.assignMemberToTeam(request.getTeam().getId(), request.getMember().getId());
        request.setStatus("Accepted");
        requestRepository.save(request);
    }

    //7
    public void memberRejectRequest(Integer member_id, Integer request_id) {
        Request request = requestRepository.findRequestByMemberIdAndRequestId(member_id, request_id);
        if (request == null) {
            throw new ApiException("request no found");
        }
        request.setStatus("Rejected");
        requestRepository.save(request);
    }


}






