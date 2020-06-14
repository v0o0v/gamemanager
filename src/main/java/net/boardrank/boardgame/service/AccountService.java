package net.boardrank.boardgame.service;

import net.boardrank.boardgame.domain.*;
import net.boardrank.boardgame.domain.repository.jpa.AccountRepository;
import net.boardrank.boardgame.domain.repository.jpa.FriendRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    NoticeService noticeService;

    @Autowired
    FriendRepository friendRepository;

    @Value("${net.boardrank.friend.max}")
    Integer maxFriendNum;

    private Collection<? extends GrantedAuthority> authorities(Set<AccountRole> roles) {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                .collect(Collectors.toSet());
    }

    @Transactional
    public Account saveAccount(Account account) {
        return this.accountRepository.save(account);
    }

    @Transactional
    public boolean isExistEmail(String email) {
        return this.accountRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public boolean isExistName(String name) {
        return this.accountRepository.findByName(name).isPresent();
    }

    @Transactional(readOnly = true)
    public Account getAccount(Long id) {
        return this.accountRepository.getOne(id);
    }

    @Transactional
    public Page<Account> getAccounts(Pageable pageable) {
        return this.accountRepository.findAll(pageable);
    }

    @Transactional
    public List<Account> getAllByEmailContaining(String txt) {
        return this.accountRepository.getAllByEmailContaining(txt);
    }

    @Transactional
    public Account getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticatedPrincipal principal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();

        Optional<Account> account = accountRepository.findByEmail(principal.getAttribute("email"));
        if (!account.isPresent()) {
            Account newAccount = addNewAccount(principal.getAttribute("email")
                    , principal.getAttribute("name")
                    , principal.getAttribute("picture")
                    , principal.getAttribute("locale")
            );
            return newAccount;
        }
        return account.get();
    }

    @Transactional
    public Account syncProfileImage(Account account) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticatedPrincipal principal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();

        account = accountRepository.findByEmail(account.getEmail()).orElseThrow(RuntimeException::new);
        account.setPictureURL(principal.getAttribute("picture"));

        return this.saveAccount(account);
    }

    @Transactional
    public Account addNewAccount(String email, String name, String picture, String locale) {
        if (isExistEmail(email))
            throw new RuntimeException("이미 있는 email입니다.");

        Account account = new Account(email, name, picture, locale);

        return this.accountRepository.save(account);
    }

    @Transactional
    public Set<Account> getAccountsContainsName(String value) {
        return this.accountRepository.findAllByNameContains(value);
    }

    @Transactional
    public void requestFriend(Account fromAccount, Account toAccount) {
        fromAccount = getAccount(fromAccount.getId());
        FriendStatus friendStatus = getFriendStatus(fromAccount, toAccount);

        if (friendStatus.equals(FriendStatus.Friend))
            throw new RuntimeException("이미 친구사이입니다.");

        if (friendStatus.equals(FriendStatus.FriendRequested))
            throw new RuntimeException("이미 친구요청중입니다.");

        this.noticeService.noticeToMakeFriend(fromAccount, toAccount);
    }

    @Transactional
    public boolean isProgressMakeFriend(Account from, Account to) {
        return noticeService.isExistNotice(NoticeType.friendRequest, from, to);
    }

    @Transactional
    public void handleRequestFriend(Notice notice, NoticeResponse response) {
        switch (response) {
            case Accept:
                makeFriend(notice.getFrom(), notice.getTo());
                break;
            case Deny:
                break;
        }
        noticeService.finish(notice);
    }

    @Transactional
    public void makeFriend(Account a, Account b) {

        if (a.getFriends().size() >= maxFriendNum || b.getFriends().size() >= maxFriendNum)
            throw new RuntimeException("Exceed Max Friend Number");

        LocalDateTime now = LocalDateTime.now();

        if (!a.isFriend(b)) {
            Friend ab = new Friend(a, b, now);
            a.addFriend(ab);
            this.accountRepository.save(a);
        }

        if (!b.isFriend(a)) {
            Friend ba = new Friend(b, a, now);
            b.addFriend(ba);
            this.accountRepository.save(b);
        }
    }

    @Transactional
    public void removeFriend(Account me, Friend friend) {
        me.getFriends().remove(friend);
        accountRepository.save(me);
        friendRepository.delete(friend);
    }

    @Transactional
    public Account changeName(Account account, String newName) {
        account = this.accountRepository.findByEmail(account.getEmail()).orElseThrow(RuntimeException::new);
        account.setName(newName);
        return saveAccount(account);
    }

    @Transactional(readOnly = true)
    public FriendStatus getFriendStatus(Account from, Account to) {
        if (from.equals(to))
            return FriendStatus.Me;

        if (from.isFriend(to)) {
            return FriendStatus.Friend;
        } else {
            if (isProgressMakeFriend(from, to)) {
                return FriendStatus.FriendRequested;
            }
            return FriendStatus.NotFriend;
        }
    }
}
